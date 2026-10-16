package jammy.schedulingbatchspringtest.batch.domain;

import java.time.LocalDate;

public record Order(
        Long id,
        int amount,
        String status,
        LocalDate orderedDate
) {}
