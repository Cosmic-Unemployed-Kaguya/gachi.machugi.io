package jammy.schedulingbatchspringtest.schedulerbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Slf4j
@Configuration
public class MultiInstanceJobConfig {

    private static final long WORK_MILLIS = 3000;  // 작업 시간 (두 인스턴스의 실행이 겹치도록)

    private final long pid = ProcessHandle.current().pid();

    // Job: 인스턴스 2개에서 동시에 실행 요청될 배치 작업
    @Bean
    public Job multiInstanceJob(JobRepository jobRepository, Step multiInstanceStep) {
        return new JobBuilder("multiInstanceJob", jobRepository)
                .start(multiInstanceStep)
                .build();
    }

    // Step: 시작/종료 로그 사이에 WORK_MILLIS만큼 대기
    @Bean
    public Step multiInstanceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("multiInstanceStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
                    log.info("[batch][pid={}] 작업 시작 runAt={}", pid, params.get("runAt"));
                    Thread.sleep(WORK_MILLIS);
                    log.info("[batch][pid={}] 작업 종료 runAt={}", pid, params.get("runAt"));
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
