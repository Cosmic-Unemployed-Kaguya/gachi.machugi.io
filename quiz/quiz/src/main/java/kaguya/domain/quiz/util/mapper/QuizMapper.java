package kaguya.domain.quiz.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.request.QuizUpdateRequest;
import kaguya.domain.quiz.model.entity.QuizEntity;

@Component
public class QuizMapper {

    public QuizEntity requestToEntity(QuizCreateRequest request) {
        return QuizEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(request.getThumbnail())
                .category(request.getCategory())
                .creatorId(request.getCreatorId())
                .build();
    }

    public void updateEntity(QuizEntity quizEntity, QuizUpdateRequest request) {
        quizEntity.updateQuiz(
                request.getTitle(),
                request.getDescription(),
                request.getThumbnail(),
                request.getCategory()
        );
    }
}