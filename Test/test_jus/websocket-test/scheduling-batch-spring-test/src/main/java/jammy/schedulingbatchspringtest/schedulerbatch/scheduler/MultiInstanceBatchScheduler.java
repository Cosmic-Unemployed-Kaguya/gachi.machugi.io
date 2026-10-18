package jammy.schedulingbatchspringtest.schedulerbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
//@Component
public class MultiInstanceBatchScheduler {

    /**
     * [스케줄러 + 배치 2단계] 멀티 인스턴스에서 한 번만 실행
     * - 서버를 여러 대 띄우면 스케줄러도 서버마다 동작해 같은 배치가 서버 수만큼 실행 요청됨
     * - 분산 락 없이 JobInstance만으로 막히는지(실험A), 분산 락을 붙이면 어떻게 달라지는지(실험B) 비교
     *
     * 실행 방법
     * - BatchLaunchScheduler(1단계)의 @Component 주석 처리
     * - 스케줄러 4단계와 같은 방법으로 인스턴스 2개 실행
     *   - (실험A) 락 없음: @SchedulerLock 주석 처리
     *   - (실험B) 락 있음: @SchedulerLock 주석 해제
     *
     * 결과
     * - (실험A) 매 주기 한 인스턴스만 실행, 다른 쪽은 실행 거부 (CannotAcquireLockException)
     *   : 두 인스턴스가 같은 runAt으로 동시에 JobInstance를 만들려 해서 DB가 한쪽 트랜잭션을 취소
     *   - 중복 실행은 막히지만 매번 양쪽이 실행 요청을 보내고, 진 쪽에 SQL 오류 로그가 남음
     *   - 식별 파라미터(runAt)가 정확히 같아야만 막힘 (서버 간 시계 차이로 값이 달라지면 둘 다 실행)
     * - (실험B) 매 주기 락을 잡은 인스턴스만 실행 요청, 다른 쪽은 "Not executing 'multiInstanceJob'. It's locked."
     *   : 분산 락이 실행 요청 자체를 한 번으로 제한, DB 오류 없음
     *   - 실행 인스턴스는 고정이 아님 (Redis에 먼저 도착한 쪽이 실행)
     * - 결과 확인 (아래 SQL)
     *   - 같은 runAt에 execution 1개, requestedBy로 실행한 인스턴스(pid) 확인
     *
     * 정리
     * - 분산 락: 실행 요청을 한 번만 보내게 함 (정상 상황의 중복 방지)
     * - JobInstance: 락이 풀린 뒤 들어온 요청까지 막는 최후 방어선 (식별 파라미터가 같을 때만)
     */
/*
[실행 이력 확인]
SELECT i.job_instance_id, e.job_execution_id, e.status, p.parameter_name, p.parameter_value
FROM batch_job_instance i
JOIN batch_job_execution e ON e.job_instance_id = i.job_instance_id
JOIN batch_job_execution_params p ON p.job_execution_id = e.job_execution_id
WHERE i.job_name = 'multiInstanceJob'
ORDER BY e.job_execution_id, p.parameter_name;
*/

    private static final String ZONE_ID = "Asia/Seoul";

    private final JobOperator jobOperator;
    private final Job multiInstanceJob;
    private final long pid = ProcessHandle.current().pid();

    public MultiInstanceBatchScheduler(
            JobOperator jobOperator,
            @Qualifier("multiInstanceJob") Job multiInstanceJob
    ) {
        this.jobOperator = jobOperator;
        this.multiInstanceJob = multiInstanceJob;
    }

    @Scheduled(cron = "0/20 * * * * *", zone = ZONE_ID)
    @SchedulerLock(
            name = "multiInstanceJob",
            lockAtMostFor = "15s",
            lockAtLeastFor = "5s"
    )
    public void launch() {
        // Job에 넘길 파라미터 생성
        JobParameters params = new JobParametersBuilder()
                // 실행 시각 (초 단위로 잘라 같은 주기의 두 인스턴스가 같은 값 → 같은 작업으로 판단)
                .addLocalDateTime("runAt", LocalDateTime.now(ZoneId.of(ZONE_ID)).truncatedTo(ChronoUnit.SECONDS))
                // 요청한 인스턴스 pid (false: 실행 여부 판단에서 제외, 어느 인스턴스가 실행했는지 기록용)
                .addString("requestedBy", String.valueOf(pid), false)
                .toJobParameters();

        log.info("[scheduler][pid={}] 실행 요청 runAt={}", pid, params.getLocalDateTime("runAt"));

        try {
            JobExecution execution = jobOperator.start(multiInstanceJob, params);
            log.info("[scheduler][pid={}] 실행 결과 status={}", pid, execution.getStatus());
        } catch (Exception e) {
            log.warn("[scheduler][pid={}] 실행 거부 {}: {}", pid, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}