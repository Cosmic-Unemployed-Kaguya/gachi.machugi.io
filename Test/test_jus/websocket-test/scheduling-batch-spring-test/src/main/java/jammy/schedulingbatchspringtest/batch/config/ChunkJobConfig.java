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
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class ChunkJobConfig {

    /**
     * [3단계] Chunk 처리 (Reader → Processor → Writer)
     * - 대량 데이터를 한 번에 처리하지 않고, CHUNK_SIZE개씩 읽기 → 가공 → 쓰기를 반복
     * - 주문(orders)을 읽어 정산(settlement)으로 저장하며, 청크 단위로 커밋되는지 확인
     *
     * 실행 방법
     * - orders(25건, 그중 5건 CANCELED), settlement 테이블 생성
     * - application.yml의 spring.batch.job.enabled를 true로 변경
     * - application.yml의 spring.batch.job.name을 chunkJob으로 변경 후 실행
     *
     * 결과 (CHUNK_SIZE = 10 기준)
     * - 로그: processor 10건 → writer 1번이 3번 반복 (8건, 8건, 4건 INSERT)
     *   : 취소 주문은 Processor에서 제외되어 Writer로 넘어가지 않음
     * - SELECT COUNT(*) FROM settlement;  → 20
     * - SELECT read_count, filter_count, write_count, commit_count
     *   FROM batch_step_execution WHERE step_name = 'settlementStep';
     *   → 읽음 25, 제외 5, 저장 20, 커밋 3
     *   : 청크마다 트랜잭션이 따로 커밋됨
     */
/*
[orders/settlement 테이블 생성]
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY, amount INT NOT NULL,
    status VARCHAR(20) NOT NULL, ordered_date DATE NOT NULL
);
CREATE TABLE settlement (
    id BIGSERIAL PRIMARY KEY, order_id BIGINT NOT NULL,
    fee INT NOT NULL, settle_amount INT NOT NULL
);

[데이터 입력]
INSERT INTO orders (amount, status, ordered_date)
SELECT i * 1000,
       CASE WHEN i % 5 = 0 THEN 'CANCELED' ELSE 'PAID' END,
       DATE '2026-10-10'
FROM generate_series(1, 25) AS i;
*/

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job chunkJob(JobRepository jobRepository, Step settlementStep) {
        return new JobBuilder("chunkJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    // Step: Reader → Processor → Writer를 CHUNK_SIZE 단위로 반복 (청크마다 커밋)
    @Bean
    public Step settlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcCursorItemReader<Order> orderReader,
            ItemProcessor<Order, Settlement> settlementProcessor,
            ItemWriter<Settlement> settlementWriter
    ) {
        return new StepBuilder("settlementStep", jobRepository)
                .<Order, Settlement>chunk(CHUNK_SIZE)  // <읽는 타입, 쓰는 타입>
                .reader(orderReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .transactionManager(transactionManager)  // 청크 1개 = 트랜잭션 1개
                .build();
    }

    // Reader: 처리할 데이터를 읽어 오는 역할
    @Bean
    public JdbcCursorItemReader<Order> orderReader(DataSource dataSource) {
        // orders → Order
        return new JdbcCursorItemReaderBuilder<Order>()
                .name("orderReader")
                .dataSource(dataSource)
                .sql("SELECT id, amount, status, ordered_date FROM orders ORDER BY id")
                .dataRowMapper(Order.class)  // 컬럼 → 필드 자동 매핑 (ordered_date → orderedDate)
                .build();
    }

    // Processor: 읽은 데이터를 가공하는 역할, 비즈니스 로직이 들어가는 곳
    @Bean
    public ItemProcessor<Order, Settlement> settlementProcessor() {
        // Order → Settlement, null이면 제외
        return order -> {
            if ("CANCELED".equals(order.status())) {
                log.info("[processor] order {} -> 취소 주문, 제외", order.id());
                return null;
            }

            int fee = order.amount() * 3 / 100;
            log.info("[processor] order {} → 정산 (금액 {}, 수수료 {})", order.id(), order.amount(), fee);
            return new Settlement(order.id(), fee, order.amount() - fee);
        };
    }

    // Writer: 가공된 데이터를 저장하는 역할
    @Bean
    public ItemWriter<Settlement> settlementWriter(DataSource dataSource) {
        // 청크 단위로 모인 정산 데이터를 한 번에 INSERT
        JdbcBatchItemWriter<Settlement> jdbcWriter = new JdbcBatchItemWriterBuilder<Settlement>()
                .dataSource(dataSource)
                .sql("INSERT INTO settlement (order_id, fee, settle_amount) VALUES (?, ?, ?)")
                .itemPreparedStatementSetter((settlement, ps) -> {
                    ps.setLong(1, settlement.orderId());
                    ps.setInt(2, settlement.fee());
                    ps.setInt(3, settlement.settleAmount());
                })
                .build();

        // 청크마다 몇 건이 저장되는지 로그로 확인하려고 감쌈
        return chunk -> {
            log.info("[writer] {} 건 INSERT", chunk.size());
            jdbcWriter.write(chunk);
        };
    }
}