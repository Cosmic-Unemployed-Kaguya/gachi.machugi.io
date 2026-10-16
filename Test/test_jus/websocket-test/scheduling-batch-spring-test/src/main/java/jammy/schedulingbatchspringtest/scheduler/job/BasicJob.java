package jammy.schedulingbatchspringtest.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;

@Slf4j
//@Component
public class BasicJob {

    /**
     * [1단계] 기본동작 테스트 (@Scheduled 주석을 하나씩 해제하며 테스트)
     * - 정해진 주기나 시각에 메서드를 자동 실행하는 @Scheduled의 세 가지 방식 비교
     * - fixedRate는 시작 기준, fixedDelay는 종료 기준이라 작업이 오래 걸릴 때 차이가 남
     *
     * - fixedRateJob(): fixedRate 테스트
     * - fixedDelayJob(): fixedDelay 테스트
     * - cronJob(): cron 테스트
     */

    // fixedRate — (이전 작업이 시작된 기준) 3초마다
//    @Scheduled(fixedRate  = 3000)
    public void fixedRateJob() throws InterruptedException {
        log.info("[fixedRate][{}] start: {}", Thread.currentThread().getName(), LocalTime.now());
        Thread.sleep(2000);  // 작업이 2초 걸림
        log.info("[fixedRate][{}]   end: {} \n", Thread.currentThread().getName(), LocalTime.now());
    }

    // fixedDelay — (이전 작업이 끝난 시각 기준) 3초 뒤
//    @Scheduled(fixedDelay = 3000)
    public void fixedDelayJob() throws InterruptedException {
        log.info("[fixedDelay][{}] start: {}", Thread.currentThread().getName(), LocalTime.now());
        Thread.sleep(2000);  // 작업이 2초 걸림
        log.info("[fixedDelay][{}]   end: {} \n", Thread.currentThread().getName(), LocalTime.now());
    }

    // cron — (특정 시각) 매 00초, 10초, 20초 ...
    @Scheduled(cron = "*/10 * * * * *")
    public void cronJob() throws InterruptedException {
        log.info("[cron][{}] start: {}", Thread.currentThread().getName(), LocalTime.now());
        Thread.sleep(2000);  // 작업이 2초 걸림
        log.info("[cron][{}]   end: {} \n", Thread.currentThread().getName(), LocalTime.now());
    }
}
