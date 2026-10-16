package jammy.schedulingbatchspringtest.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;

@Slf4j
//@Component
public class LockJob {

    /**
     * [5단계] ShedLock + Redis 분산 락으로 중복 실행 방지
     * - 4단계의 중복 실행을 막기 위해, 인스턴스들이 공유하는 Redis에 락을 걸어 한 대만 실행
     * - 락을 못 잡은 인스턴스는 기다리지 않고 이번 실행을 건너뜀
     *
     * 실행 방법
     * - Redis 실행
     * - 4단계와 같은 방법으로 인스턴스 2개 실행
     * - redis-cli에서 MONITOR로 락 명령 확인
     *
     * 결과
     * - 매 주기마다 한 인스턴스만 실행, 나머지는 "Not executing 'lockJob'. It's locked." 로 건너뜀
     * - 실행 인스턴스는 고정이 아님 (Redis에 먼저 도착한 쪽이 실행)
     * - 한 인스턴스를 종료해도 남은 인스턴스가 계속 실행
     *
     * Redis 동작
     * - 락 획득: SET key value PX 9000 NX  (키가 없을 때만 저장, 만료 = lockAtMostFor)
     * - 작업 종료: SET key value PX (남은 lockAtLeastFor) XX  (바로 삭제하지 않고 최소 유지 시간까지 남김)
     * - 락 해제: 키가 만료되어 사라지면 해제된 것
     *
     * 시간 설정 기준
     * - 작업 시간 < lockAtMostFor < 실행 주기
     * - lockAtLeastFor <= lockAtMostFor
     */

    private final long pid = ProcessHandle.current().pid();

    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")
    @SchedulerLock(
            name = "lockJob",   // Redis 키 이름에 사용, 같은 name끼리는 동시에 하나만 실행
            lockAtMostFor = "9s",  // 실행 서버가 죽어도 이 시간 뒤 락 해제 (다음 주기 10초 전에 풀리도록)
            lockAtLeastFor = "5s"  // 작업이 바로 끝나도 이 시간까지 락 유지 (서버 간 시계 오차로 인한 재실행 방지)
    )
    public void lockJob() {
        LockAssert.assertLocked();  // 락 없이 실행되면 예외
        log.info("[lock][pid={}] run: {}", pid, LocalTime.now());
    }
}