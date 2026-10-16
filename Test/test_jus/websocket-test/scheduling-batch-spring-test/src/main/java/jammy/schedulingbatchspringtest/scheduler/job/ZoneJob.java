package jammy.schedulingbatchspringtest.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;

@Slf4j
//@Component
public class ZoneJob {

    /**
     * [3단계] cron 타임존(zone) 테스트
     * - cron은 zone을 지정하지 않으면 JVM 기본 타임존 기준으로 해석됨
     * - 로컬(KST)과 서버(UTC)처럼 타임존이 다를 때 실행 시각이 어떻게 달라지는지 확인
     *
     * 실행 방법
     * - application.yml의 zone-test.cron을 현재 한국 시간으로 맞추고 실행
     * - 실험 A: 그대로 실행 (JVM 기본 타임존 = OS 설정 = Asia/Seoul)
     * - 실험 B: VM 옵션 -Duser.timezone=UTC 추가 후 실행 (UTC 서버/컨테이너 환경 흉내)
     *   - 적용 위치: Run/Debug Configurations → Modify options → Add VM options 추가 → -Duser.timezone=UTC
     *
     * 결과
     * - (실험 A) default, seoul 실행 / utc 실행 안 됨
     *   : JVM 기본 타임존이 Asia/Seoul이라 default와 seoul이 같은 기준으로 동작함
     * - (실험 B) seoul만 실행 / default, utc 실행 안 됨
     *   : JVM 기본 타임존이 UTC로 바뀌어 default도 UTC 기준으로 cron을 해석함
     *   : 코드는 그대로인데 서버 타임존만 바뀌어도 zone 미지정 작업은 9시간 어긋남
     */

    // zone 미지정: JVM 기본 타임존 기준 (-Duser.timezone 옵션이 없으면 OS 시간대 설정)
    @Scheduled(cron = "${zone-test.cron}")
    public void defaultZoneJob() {
        log.info("[default] run: {}", ZonedDateTime.now());
    }

    // zone = 한국 시간 기준 (IANA tz database 기준)
    @Scheduled(cron = "${zone-test.cron}", zone = "Asia/Seoul")
    public void seoulZoneJob() {
        log.info("[seoul] run: {}", ZonedDateTime.now());
    }

    // zone = UTC 기준
    @Scheduled(cron = "${zone-test.cron}", zone = "UTC")
    public void utcZoneJob() {
        log.info("[UTC] run: {}", ZonedDateTime.now());
    }
}