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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class SkipRetryJobConfig {

    /**
     * [5단계] Skip / Retry
     * - 4단계처럼 한 건의 오류로 Step 전체가 실패하지 않도록, 오류 종류에 따라 건너뛰거나(Skip) 다시 시도(Retry)
     * - 정상 처리, Skip 한도 초과, Retry 한도 초과 각각의 동작 확인
     *
     * 실행 방법
     * - application.yml의 spring.batch.job.enabled를 true로 변경
     * - application.yml의 spring.batch.job.name을 skipRetryJob으로 변경
     * - application.yml의 batch-test 값을 실험별로 변경
     *   - (실험A) 기본: skip-order-ids 3,13 / retry-fail-count 2
     *   - (실험B) Skip 한도 초과: skip-order-ids 3,13,23 / retry-fail-count 2
     *   - (실험C) Retry 한도 초과: skip-order-ids 3,13 / retry-fail-count 5
     * - 실험마다 TRUNCATE settlement 후 date를 새 값으로 실행
     *
     * 결과
     * - (실험A) COMPLETED
     *   - skip 대상은 건너뛰고, 일시적 오류는 재시도해서 성공 : 오류가 SKIP_LIMIT, RETRY_LIMIT 안에 있음
     *   - 로그 : retry 대상이 "3번째 시도"에 성공
     *   - SQL : process_skip_count 2, settlement 18
     * - (실험B) FAILED: SkipLimitExceededException
     *   - SKIP_LIMIT을 넘는 오류에서 멈추고 그 청크는 롤백 : 오류가 많으면 데이터나 코드 문제로 보고 중단
     *   - SQL : process_skip_count 2, commit_count 2, rollback_count 1, settlement 14
     * - (실험C) FAILED: NonSkippableProcessException
     *   - 첫 시도 + RETRY_LIMIT번 재시도 후 실패 : 재시도를 다 쓰면 skip 정책으로 넘어가는데 skip 대상이 아님
     *   - 로그 : "1번째 시도" ~ "4번째 시도"가 약 1초 간격으로 출력
     *   - SQL : commit_count 0, rollback_count 1, settlement 0
     * - 결과 확인 (아래 SQL)
     *   - 정산 건수 → A 18 / B 14 / C 0
     *   - Step 실행 결과 (read / filter / write / skip / commit / rollback)
     *     → 실험A: 25 / 5 / 18 / 2 / 3 / 0
     *     → 실험B: 25 / 4 / 14 / 2 / 2 / 1
     *     → 실험C: 10 / 1 / 0 / 1 / 0 / 1
     */
/*
[정산 건수 확인]
SELECT COUNT(*) FROM settlement;

[Step 실행 결과 확인]
SELECT status, read_count, filter_count, write_count, process_skip_count, commit_count, rollback_count
FROM batch_step_execution
WHERE step_name = 'skipRetryStep'
ORDER BY step_execution_id DESC LIMIT 1;
*/

    private static final int CHUNK_SIZE = 10;
    private static final int SKIP_LIMIT = 2;  // 건너뛸 수 있는 최대 건수 (넘으면 Job 실패)
    private static final int RETRY_LIMIT = 3;  // 재시도 횟수 (첫 시도 + RETRY_LIMIT번)

    // 데이터 오류: 다시 해도 실패 → Skip 대상
    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) { super(message); }
    }

    // 일시적 오류: 다시 하면 성공할 수 있음 → Retry 대상
    public static class TemporaryException extends RuntimeException {
        public TemporaryException(String message) { super(message); }
    }

    @Bean
    public Job skipRetryJob(JobRepository jobRepository, Step skipRetryStep) {
        return new JobBuilder("skipRetryJob", jobRepository)
                .start(skipRetryStep)
                .build();
    }

    // Step: 3단계 Reader/Writer 재사용, Processor만 오류 발생용으로 교체 + Skip/Retry 설정
    @Bean
    public Step skipRetryStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<Order> orderReader,  // 3단계(ChunkJobConfig) Reader 재사용
            ItemProcessor<Order, Settlement> faultyProcessor,  // 같은 타입 빈이 여러 개라 파라미터 이름으로 구분
            ItemWriter<Settlement> settlementWriter  // 3단계(ChunkJobConfig) Writer 재사용
    ) {
        return new StepBuilder("skipRetryStep", jobRepository)
                .<Order, Settlement>chunk(CHUNK_SIZE)
                .reader(orderReader)
                .processor(faultyProcessor)
                .writer(settlementWriter)
                .transactionManager(transactionManager)
                .faultTolerant()  // Skip/Retry 기능 켜기
                .skip(InvalidOrderException.class)  // 이 예외는 그 건만 건너뜀
                .skipLimit(SKIP_LIMIT)
                .retry(TemporaryException.class)  // 이 예외는 다시 시도
                .retryLimit(RETRY_LIMIT)
                .build();
    }

    // Processor: yml에 지정한 주문에서 일부러 오류를 내고, 나머지는 3단계 Processor에 그대로 넘김
    @Bean
    public ItemProcessor<Order, Settlement> faultyProcessor(
            @Value("${batch-test.skip-order-ids:}") List<Long> skipOrderIds,  // 데이터 오류를 낼 주문들
            @Value("${batch-test.retry-order-id:0}") long retryOrderId,  // 일시적 오류를 낼 주문
            @Value("${batch-test.retry-fail-count:0}") int retryFailCount,  // 몇 번 실패한 뒤 성공할지
            ItemProcessor<Order, Settlement> settlementProcessor
    ) {
        Map<Long, AtomicInteger> attempts = new ConcurrentHashMap<>();  // 주문별 시도 횟수

        return order -> {
            if (skipOrderIds.contains(order.id())) {
                log.warn("[processor] order {} → 데이터 오류 (Skip 대상)", order.id());
                throw new InvalidOrderException("order " + order.id() + " 데이터 오류");
            }

            if (order.id() == retryOrderId) {
                int attempt = attempts.computeIfAbsent(order.id(), id -> new AtomicInteger()).incrementAndGet();
                if (attempt <= retryFailCount) {
                    log.warn("[processor] order {} → 일시적 오류 ({}번째 시도)", order.id(), attempt);
                    throw new TemporaryException("order " + order.id() + " 일시적 오류");
                }
                log.info("[processor] order {} → {}번째 시도에서 성공", order.id(), attempt);
            }

            return settlementProcessor.process(order);
        };
    }
}