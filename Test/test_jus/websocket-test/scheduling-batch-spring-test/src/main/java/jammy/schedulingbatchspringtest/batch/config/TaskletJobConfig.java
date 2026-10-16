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

@Slf4j
@Configuration
public class TaskletJobConfig {

    /**
     * [1단계] Tasklet Job 실행 + 배치 메타데이터 확인
     * - 배치의 가장 단순한 형태(Job → Step → Tasklet)를 실행
     * - 실행할 때마다 DB(batch_ 테이블)에 실행 이력이 남는지 확인
     *
     * 실행 방법
     * - application.yml의 spring.batch.job.enabled를 true로 변경
     * - application.yml의 spring.batch.job.name을 taskletJob으로 변경 후 실행
     *
     * 결과
     * - 로그: Job → Step → Tasklet 순서로 실행되고 "status: [COMPLETED]"로 종료
     * - psql \dt: batch_ 테이블 6개 생성
     * - SELECT * FROM batch_job_instance;  → taskletJob 1행
     *   SELECT * FROM batch_job_execution;  → status = COMPLETED
     *   SELECT * FROM batch_step_execution;  → taskletStep, commit_count = 1
     *   : 배치는 실행할 때마다 "언제, 어떤 Job이, 어떻게 끝났는지"를 DB에 남김
     */

    // Job: 배치 작업 전체 (Step들을 순서대로 실행)
    @Bean
    public Job taskletJob(JobRepository jobRepository, Step taskletStep) {
        return new JobBuilder("taskletJob", jobRepository)
                .start(taskletStep)  // 첫 번째로 실행할 Step (여러 개면 .next()로 연결)
                .build();
    }

    // Step: Tasklet 방식 (메서드 하나 실행으로 끝나는 단계)
    @Bean
    public Step taskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStep", jobRepository)
                // 이 Step에서 실행할 작업
                .tasklet((contribution, chunkContext) -> {
                    log.info("[taskletJob] taskletStep 실행");
                    return RepeatStatus.FINISHED;  // 한 번 실행하고 종료 (CONTINUABLE이면 다시 실행)
                }, transactionManager)  // 작업을 트랜잭션으로 감쌈 (성공 시 커밋, 예외 시 롤백)
                .build();
    }
}