package kaguya.domain.answer.model.dto.response;

import java.time.OffsetDateTime;

import kaguya.domain.answer.model.entity.AnswerEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerResponse {

    private Long idx;
    private Long questionIdx;
    private String answer;
    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

    public static AnswerResponse fromEntity(AnswerEntity answerEntity) {
        return AnswerResponse.builder()
                .idx(answerEntity.getIdx())
                .questionIdx(answerEntity.getQuestionIdx())
                .answer(answerEntity.getAnswer())
                .createdDate(answerEntity.getCreatedDate())
                .updatedDate(answerEntity.getUpdatedDate())
                .build();
    }
}