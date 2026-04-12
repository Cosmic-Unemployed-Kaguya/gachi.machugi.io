package kaguya.domain.quiz.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

import kaguya.domain.quiz.model.entity.QuizEntity;

@Getter
@Builder
public class QuizResponse {

    private Long idx;
    private String title;
    private String description;
    private Integer questionCount;
    private String thumbnail;
    private Long creatorId;
    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;
    private String category;

    public static QuizResponse from(QuizEntity quizEntity) {
        return QuizResponse.builder()
                .idx(quizEntity.getIdx())
                .title(quizEntity.getTitle())
                .description(quizEntity.getDescription())
                .questionCount(quizEntity.getQuestionCount())
                .thumbnail(quizEntity.getThumbnail())
                .creatorId(quizEntity.getCreatorId())
                .createdDate(quizEntity.getCreatedDate())
                .updatedDate(quizEntity.getUpdatedDate())
                .category(quizEntity.getCategory())
                .build();
    }
}