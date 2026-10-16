package jammy.schedulingbatchspringtest.batch.config;

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
public class ParameterJobConfig {

    /**
     * [2단계] JobParameters와 JobInstance
     * - 같은 Job이라도 넘긴 파라미터에 따라 다른 작업(JobInstance)으로 구분됨
     * - 같은 파라미터로 다시 실행하거나, 파라미터를 바꿔 실행했을 때의 동작 확인
     *
     * 실행 방법
     * - application.yml의 spring.batch.job.enabled를 true로 변경
     * - application.yml의 spring.batch.job.name을 parameterJob으로 변경
     * - Program arguments에 파라미터를 바꿔 가며 실행
     *   - date=2026-10-11
     *   - date=2026-10-12 requestedBy=jammy,java.lang.String,false (마지막 false = 식별 안 함)
     *
     * 결과
     * - 같은 date로 재실행 → 로그에 JobInstanceAlreadyCompleteException, 앱 종료
     *   : 이미 성공한 작업(Job + 파라미터)은 다시 실행되지 않음
     * - 다른 date로 실행 → 새 작업으로 실행됨
     * - requestedBy만 다르게 실행 → 실행 거부
     *   : 식별하지 않는 파라미터는 같은 작업인지 판단할 때 쓰이지 않음
     * - SELECT * FROM batch_job_instance;          → 성공한 date마다 1행 (job_key가 다름)
     *   SELECT * FROM batch_job_execution_params;  → 넘긴 파라미터, identifying = Y/N
     */

    @Bean
    public Job parameterJob(JobRepository jobRepository, Step parameterStep) {
        return new JobBuilder("parameterJob", jobRepository)
                .start(parameterStep)
                .build();
    }

    @Bean
    public Step parameterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parameterStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실행 시 넘긴 파라미터 꺼내기
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
                    log.info("[parameterJob] params={}", params);
                    log.info("[parameterJob] date={}", params.get("date"));
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
