package kaguya.domain.answer.model.dto.response;

import java.time.OffsetDateTime;

public record AnswerResponse (
        Long idx,
        Long questionIdx,
        String answer,
        OffsetDateTime createdDate,
        OffsetDateTime updatedDate
){
}