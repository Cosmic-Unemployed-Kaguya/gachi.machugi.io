package jammy.schedulingbatchspringtest.batch.config;

import jammy.schedulingbatchspringtest.batch.domain.Order;
import jammy.schedulingbatchspringtest.batch.domain.Settlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class RestartJobConfig {

    /**
     * [4단계] 실패와 재시작
     * - 배치는 처리 도중 실패할 수 있음 (데이터 오류, DB 장애 등)
     * - 실패했을 때 어디까지 저장되는지, 같은 파라미터로 다시 실행하면 어디서부터 이어지는지 확인
     *
     * 실행 방법
     * - application.yml의 spring.batch.job.enabled를 true로 변경
     * - application.yml의 spring.batch.job.name을 restartJob으로 변경
     * - (SQL) TRUNCATE settlement; 후 실행
     * - 1차: batch-test.fail-order-id = 15 로 실행 (15번 주문에서 예외 발생)
     * - 2차: batch-test.fail-order-id = 0 으로 바꾸고, 같은 Program arguments(date)로 재실행
     *
     * 결과 (CHUNK_SIZE = 10 기준)
     * - 1차: 청크 1(1~10번) 커밋 후 청크 2에서 예외 → FAILED, settlement 8건
     *   : 이미 커밋한 청크는 남고, 실패한 청크만 롤백됨
     * - 2차: 로그가 order 11부터 시작 → COMPLETED, settlement 20건 (중복 없음)
     *   : 실패한 Job은 같은 파라미터로 재실행하면 마지막으로 커밋한 위치부터 이어서 처리됨
     * - 결과 확인 (아래 SQL)
     *   → 1차 read 20 / write 8 / rollback 1, 2차 read 15 / write 12
     */
/*
[결과 확인용 SQL]
SELECT e.job_execution_id, e.status AS job_status,
s.read_count, s.write_count, s.commit_count, s.rollback_count
FROM batch_job_instance i
JOIN batch_job_execution e ON e.job_instance_id = i.job_instance_id
JOIN batch_step_execution s ON s.job_execution_id = e.job_execution_id
WHERE i.job_name = 'restartJob'
ORDER BY e.job_execution_id;
*/

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job restartJob(JobRepository jobRepository, Step restartStep) {
        return new JobBuilder("restartJob", jobRepository)
                .start(restartStep)
                .build();
    }

    @Bean
    public Step restartStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<Order> orderReader,  // 3단계(ChunkJobConfig) Reader 재사용
            ItemProcessor<Order, Settlement> failingProcessor,  // 같은 타입 빈이 여러 개라 파라미터 이름으로 구분
            ItemWriter<Settlement> settlementWriter  // 3단계(ChunkJobConfig) Writer 재사용
    ) {
        return new StepBuilder("restartStep", jobRepository)
                .<Order, Settlement>chunk(CHUNK_SIZE)
                .reader(orderReader)
                .processor(failingProcessor)
                .writer(settlementWriter)
                .transactionManager(transactionManager)
                .build();
    }

    // Processor: 지정한 주문 번호에서만 일부러 예외, 나머지는 3단계 Processor(settlementProcessor)에 그대로 넘김
    @Bean
    public ItemProcessor<Order, Settlement> failingProcessor(
            @Value("${batch-test.fail-order-id:0}") long failOrderId,  // yml 값 (없으면 0 = 실패 안 함)
            ItemProcessor<Order, Settlement> settlementProcessor
    ) {
        // 지정한 주문 번호에서 일부러 예외를 던지기
        return order -> {
            if (order.id() == failOrderId) {
                throw new IllegalStateException("[processor] order " + order.id() + " 처리 중 실패 (테스트)");
            }
            return settlementProcessor.process(order);
        };
    }
}