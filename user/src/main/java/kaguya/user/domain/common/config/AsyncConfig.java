package kaguya.user.domain.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "mailSendExecutor")
    public Executor mailSendExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 아래의 값(5, 30, 100)은 테스트용이고, 실제 하드웨어 스팩에 맞게 값을 지정
        executor.setCorePoolSize(5);  // 기본적인 대기 상태의 스레드 수
        executor.setMaxPoolSize(30);  // 최대 생성 가능한 쓰레드 수
        executor.setQueueCapacity(100);  // Core 쓰레드가 모두 사용중일 때 대기할 큐의 크기
        executor.setThreadNamePrefix("MailAsync-");  // 스레드 이름 접두사
        executor.initialize();

        return executor;
    }
}