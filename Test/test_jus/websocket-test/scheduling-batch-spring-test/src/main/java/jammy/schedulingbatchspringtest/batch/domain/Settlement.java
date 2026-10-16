package jammy.schedulingbatchspringtest.batch.domain;

public record Settlement (
        Long orderId,
        int fee,
        int settleAmount
) {}
