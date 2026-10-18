package kaguya.domain.quiz.util.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.response.QuizResponse;
import kaguya.domain.quiz.model.entity.QuizEntity;

@Component
public class QuizMapper {

    public QuizEntity requestToEntity(QuizCreateRequest request) {
        return QuizEntity.builder()
                .title(request.title())
                .description(request.description())
                .thumbnail(request.thumbnail())
                .category(request.category())
                .creatorId(request.creatorId())
                .build();
    }

    public QuizResponse entityToResponse(QuizEntity quizEntity) {
        return new QuizResponse(
                quizEntity.getIdx(),
                quizEntity.getTitle(),
                quizEntity.getDescription(),
                quizEntity.getQuestionCount(),
                quizEntity.getThumbnail(),
                quizEntity.getCreatorId(),
                quizEntity.getCreatedDate(),
                quizEntity.getUpdatedDate(),
                quizEntity.getCategory()
        );
    }
}