package kaguya.domain.question.model.dto.response;

import java.time.OffsetDateTime;

public record QuestionResponse(
        Long idx,
        Long quizIdx,
        String problemText,
        String problemUrl,
        String type,
        Integer sortOrder,
        OffsetDateTime createdDate,
        OffsetDateTime updatedDate
) {
}