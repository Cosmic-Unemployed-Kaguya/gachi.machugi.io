package kaguya.domain.question.model.dto.response;

import java.time.OffsetDateTime;

import kaguya.domain.question.model.entity.QuestionEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponse {
    private Long idx;
    private Long quizIdx;
    private String problemText;
    private String problemUrl;
    private String type;
    private Integer sortOrder;
    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

    public static QuestionResponse fromEntity(QuestionEntity question) {
        return QuestionResponse.builder()
                .idx(question.getIdx())
                .quizIdx(question.getQuizIdx())
                .problemText(question.getProblemText())
                .problemUrl(question.getProblemUrl())
                .type(question.getType())
                .sortOrder(question.getSortOrder())
                .createdDate(question.getCreatedDate())
                .updatedDate(question.getUpdatedDate())
                .build();
    }
}
