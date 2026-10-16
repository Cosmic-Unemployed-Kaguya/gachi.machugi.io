package jammy.schedulingbatchspringtest.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;

@Slf4j
//@Component
public class ThreadPoolJob {

    /**
     * [2단계] 스케줄러 스레드 풀 크기에 따른 작업 간 블로킹 테스트
     * - 스케줄러 기본 스레드는 1개라, 한 작업이 오래 걸리면 다른 작업까지 밀림
     * - 스레드 풀 크기를 늘리면 작업끼리 독립적으로 실행되는지 확인
     *
     * 실행 방법
     * - application.yml의 spring.task.scheduling.pool.size를 1, 2로 바꿔 가며 실행
     *
     * 결과
     * - (pool 1개) slow, fast 모두 scheduling-1에서 실행
     *   : slow가 실행되는 5초 동안 fast는 실행 가능한 시점이 와도 스레드가 없어 대기함
     * - (pool 2개) scheduling-1, scheduling-2로 나뉘어 실행
     *   : slow가 실행 중이어도 fast는 남은 스레드에서 1초마다 실행함
     */

    // 오래 걸리는 작업 (5초)
    @Scheduled(fixedDelay = 1000)
    public void slowJob() throws InterruptedException {
        log.info("[slow][{}] start: {}", Thread.currentThread().getName(), LocalTime.now());
        Thread.sleep(5000);  // 작업이 5초 걸림
        log.info("[slow][{}] end {} \n", Thread.currentThread().getName(), LocalTime.now());
    }

    // 짧은 작업
    @Scheduled(fixedDelay = 1000)
    public void fastJob() {
        log.info("[fast][{}] run {} \n", Thread.currentThread().getName(), LocalTime.now());
        // 작업 바로 끝남
    }
}