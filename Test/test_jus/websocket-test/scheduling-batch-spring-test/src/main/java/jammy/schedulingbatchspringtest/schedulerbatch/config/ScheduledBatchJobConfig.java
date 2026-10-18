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
public class ScheduledBatchJobConfig {

    // Job: 스케줄러가 실행할 배치 작업
    @Bean
    public Job scheduledBatchJob(JobRepository jobRepository, Step scheduledBatchStep) {
        return new JobBuilder("scheduledBatchJob", jobRepository)
                .start(scheduledBatchStep)
                .build();
    }

    // Step: 받은 파라미터와 실행 스레드만 출력
    @Bean
    public Step scheduledBatchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // 스케줄러가 jobOperator.start()에 넘긴 파라미터
        return new StepBuilder("scheduledBatchStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
                    log.info("[batch] 실행 date={}, requestedAt={}, thread={}",
                            params.get("date"), params.get("requestedAt"), Thread.currentThread().getName());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
