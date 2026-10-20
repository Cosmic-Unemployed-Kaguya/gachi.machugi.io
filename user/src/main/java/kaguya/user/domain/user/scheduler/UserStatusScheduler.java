package kaguya.user.domain.user.scheduler;

import kaguya.user.domain.user.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusScheduler {

    // 한국(서울) 시간 기준
    private static final String ZONE_ID = "Asia/Seoul";

    private final UserStatusService userStatusService;

    // 휴면 전환: 매일 새벽 4시
    @Scheduled(cron = "0 0 4 * * *", zone = ZONE_ID)
    @SchedulerLock(
            name = "convertDormantUsers",
            lockAtMostFor = "30m",  // 실행 서버가 죽어도 이 시간 뒤 락 해제
            lockAtLeastFor = "1m"  // 작업이 바로 끝나도 이 시간까지 락 유지 (서버 간 시계 오차로 인한 재실행 방지)
    )
    public void convertDormantUsers() {

        LockAssert.assertLocked();  // 락 없이 실행되면 예외

        int count = userStatusService.convertDormantUsers();
        log.info("[scheduler] 휴면 전환 {}건", count);
    }

    // 정지 해제: 10분마다
    @Scheduled(cron = "0 */10 * * * *", zone = ZONE_ID)
    @SchedulerLock(
            name = "releaseSuspendedUsers",
            lockAtMostFor = "5m",
            lockAtLeastFor = "30s"
    )
    public void releaseSuspendedUsers() {

        LockAssert.assertLocked();

        int count = userStatusService.releaseSuspendedUsers();
        log.info("[scheduler] 정지 해제 {}건", count);
    }
}