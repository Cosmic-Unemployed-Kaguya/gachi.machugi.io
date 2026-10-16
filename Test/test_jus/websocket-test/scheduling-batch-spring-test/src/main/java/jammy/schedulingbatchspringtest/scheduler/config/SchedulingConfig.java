package jammy.schedulingbatchspringtest.scheduler.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling  // @Scheduled 활성화
@EnableSchedulerLock(defaultLockAtMostFor = "10m")  // ShedLock 활성화 (lockAtMostFor 설정 안했을때 기본값 10분)
public class SchedulingConfig {

    /**
     * 락 저장소 설정 (Redis)
     * - Spring Boot가 자동 생성한 RedisConnectionFactory 사용
     * - ShedLock이 내부에서 문자열로만 저장하므로 별도 직렬화 설정 불필요
     *
     * 저장 형식 (String 타입 + TTL)
     * - Key: job-lock:{environment}:{@SchedulerLock name}
     *           ex) job-lock:scheduling-batch-spring-test:lockJob
     * - Value: ADDED:{락 획득 시각(UTC)}@{호스트명}:{고유 ID}
     *           ex) ADDED:2026-10-08T15:58:10.015Z@DESKTOP-XXX:c6b9030b-...
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        // environment: 같은 Redis를 여러 서비스/환경이 공유할 때 같은 이름의 락끼리 충돌하지 않도록 구분
        return new RedisLockProvider(connectionFactory, "scheduling-batch-spring-test");
    }
}