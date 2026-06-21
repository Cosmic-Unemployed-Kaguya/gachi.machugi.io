package kaguya.domain.answer.service;

import java.util.List;

import kaguya.domain.answer.model.dto.request.AnswerCheckRequest;
import kaguya.domain.answer.model.dto.request.AnswerRequest;
import kaguya.domain.answer.model.dto.response.AnswerCheckResponse;
import kaguya.domain.answer.model.dto.response.AnswerResponse;

public interface AnswerService {

    AnswerResponse createAnswer(Long questionIdx, AnswerRequest request);

    List<AnswerResponse> getAnswerList(Long questionIdx);

    AnswerResponse patchAnswer(Long answerIdx, AnswerRequest request);

    void deleteAnswer(Long answerIdx);

    AnswerCheckResponse checkAnswer(
            Long questionIdx,
            AnswerCheckRequest request
    );
}