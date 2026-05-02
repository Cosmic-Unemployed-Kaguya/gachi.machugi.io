package kaguya.domain.answer.util.mapper;

import org.springframework.stereotype.Component;

import kaguya.domain.answer.model.dto.request.AnswerRequest;
import kaguya.domain.answer.model.dto.response.AnswerResponse;
import kaguya.domain.answer.model.entity.AnswerEntity;

@Component
public class AnswerMapper {

    public AnswerEntity requestToEntity(Long questionIdx, AnswerRequest request) {
        return AnswerEntity.builder()
                .questionIdx(questionIdx)
                .answer(request.answer())
                .build();
    }

    public AnswerResponse entityToResponse(AnswerEntity answer) {
        return new AnswerResponse(
                answer.getIdx(),
                answer.getQuestionIdx(),
                answer.getAnswer(),
                answer.getCreatedDate(),
                answer.getUpdatedDate()
        );
    }
}