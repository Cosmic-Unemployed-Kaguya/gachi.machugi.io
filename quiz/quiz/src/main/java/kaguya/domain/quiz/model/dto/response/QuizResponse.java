package kaguya.domain.quiz.model.dto.response;

import java.time.OffsetDateTime;

public record QuizResponse(
        Long idx,
        String title,
        String description,
        Integer questionCount,
        String thumbnail,
        Long creatorId,
        OffsetDateTime createdDate,
        OffsetDateTime updatedDate,
        String category
) {}