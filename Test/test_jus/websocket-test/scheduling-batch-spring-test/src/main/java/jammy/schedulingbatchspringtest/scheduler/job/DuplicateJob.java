package jammy.schedulingbatchspringtest.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;

@Slf4j
//@Component
public class DuplicateJob {

    /**
     * [4단계] 멀티 인스턴스 중복 실행 재현
     * - 스케줄러는 인스턴스(JVM)마다 따로 동작해, 서버를 여러 대 띄우면 같은 작업이 서버 수만큼 실행됨
     * - 인스턴스 2개를 띄워 중복 실행을 재현
     *
     * 실행 방법
     * - Run/Debug Configurations → Modify options → Allow multiple instances 체크 후 2번 실행
     *
     * 결과
     * - 같은 시각(매 10초)에 pid가 다른 두 인스턴스에서 각각 실행됨
     *   : 스케줄러는 JVM마다 따로 돌기 때문에 서로의 실행 여부를 모름
     *
     * 유의점
     * - 인스턴스가 N대면 같은 작업이 N번 실행됨
     * - 멱등하지 않은 작업(메일 발송, 금액 지급 등)은 결과가 틀어짐 → 분산 락으로 한 대만 실행되게 해야 함
     */

    // 프로세스 ID: 인스턴스(앱)마다 다르므로 어느 인스턴스가 실행했는지 구분용
    private final long pid = ProcessHandle.current().pid();

    // 매 00초, 10초, 20초 ... 실행
    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    public void duplicateJob() {
        log.info("[duplicate][pid={}] run: {}", pid, LocalTime.now());
    }
}