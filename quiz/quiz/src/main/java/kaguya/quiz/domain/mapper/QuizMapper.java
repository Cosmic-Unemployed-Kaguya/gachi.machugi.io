package kaguya.quiz.domain.mapper;

import kaguya.quiz.domain.model.dto.request.QuizCreateRequest;
import kaguya.quiz.domain.model.entity.QuizEntity;
import org.springframework.stereotype.Component;

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
}