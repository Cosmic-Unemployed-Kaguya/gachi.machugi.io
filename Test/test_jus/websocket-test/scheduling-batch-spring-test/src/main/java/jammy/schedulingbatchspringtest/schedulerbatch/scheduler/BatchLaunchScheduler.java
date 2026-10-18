package jammy.schedulingbatchspringtest.schedulerbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
public class BatchLaunchScheduler {

    /**
     * [스케줄러 + 배치 1단계] 스케줄러로 배치 Job 실행
     * - 배치 1~5단계는 앱 시작 시 Job이 한 번 실행됐지만, 실제로는 스케줄러가 정해진 시각에 Job을 실행함
     * - 앱 시작 시 자동 실행을 끄고, @Scheduled에서 파라미터를 만들어 Job을 실행
     *
     * 실행 방법
     * - application.yml의 spring.batch.job.enabled를 false로 변경 후 실행
     *
     * 결과
     * - 첫 주기: COMPLETED
     *   - 로그 : [batch] 로그의 thread가 scheduling-N
     *     : Job이 스케줄러 스레드에서 실행되고, 끝날 때까지 스케줄러가 대기 (동기 실행)
     * - 다음 주기부터: 실행 거부 (JobInstanceAlreadyCompleteException)
     *   : 같은 date(식별 파라미터)는 이미 성공한 작업이라 다시 실행되지 않음, 스케줄은 계속 동작
     * - 결과 확인 (아래 SQL)
     *   - 실행 이력 → JobInstance 1개, Execution 1개 (COMPLETED)
     *     : 거부된 시도는 이력이 남지 않음
     *   - 파라미터 → date Y / requestedAt N
     */
/*
[실행 이력 확인]
SELECT i.job_instance_id, e.job_execution_id, e.status
FROM batch_job_instance i
LEFT JOIN batch_job_execution e ON e.job_instance_id = i.job_instance_id
WHERE i.job_name = 'scheduledBatchJob';

[파라미터 확인]
SELECT e.job_execution_id, p.parameter_name, p.parameter_value, p.identifying
FROM batch_job_execution_params p
JOIN batch_job_execution e ON e.job_execution_id = p.job_execution_id
JOIN batch_job_instance i ON i.job_instance_id = e.job_instance_id
WHERE i.job_name = 'scheduledBatchJob';
*/

    private static final String ZONE_ID = "Asia/Seoul";

    private final JobOperator jobOperator;  // Job 실행기
    private final Job scheduledBatchJob;  // 실행할 Job (ScheduledBatchJobConfig)

    // Job 빈이 여러 개라 @Qualifier로 이름을 지정하기 위해 생성자를 직접 작성
    public BatchLaunchScheduler(
            JobOperator jobOperator,
            @Qualifier("scheduledBatchJob") Job scheduledBatchJob
    ) {
        this.jobOperator = jobOperator;
        this.scheduledBatchJob = scheduledBatchJob;
    }

    @Scheduled(cron = "*/10 * * * * *", zone = ZONE_ID)
    public void launch() {
        // Job에 넘길 파라미터 생성
        JobParameters params = new JobParametersBuilder()
                .addLocalDate("date", LocalDate.now(ZoneId.of(ZONE_ID)))  // 오늘 날짜 (같은 날짜로 이미 성공했으면 실행 거부)
                .addLocalDateTime("requestedAt", LocalDateTime.now(ZoneId.of(ZONE_ID)), false)  // 요청 시각 (false: 실행 여부 판단에서 제외, 기록용)
                .toJobParameters();

        log.info("[scheduler] 실행 요청 thread={}", Thread.currentThread().getName());

        try {
            // 실행기에 Job과 파라미터를 넘겨 실행 (Job이 끝날 때까지 대기)
            JobExecution execution = jobOperator.start(scheduledBatchJob, params);
            log.info("[scheduler] 실행 결과 status={}", execution.getStatus());
        } catch (Exception e) {
            // 이미 성공한 date로 실행하면 JobInstanceAlreadyCompleteException 발생 → 로그만 남기고 다음 주기를 기다림
            log.warn("[scheduler] 실행 거부 {}: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}