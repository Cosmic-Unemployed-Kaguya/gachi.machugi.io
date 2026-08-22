package kaguya.domain.question.service;

import java.util.List;

import kaguya.domain.question.model.dto.request.QuestionRequest;
import kaguya.domain.question.model.dto.response.QuestionResponse;

public interface QuestionService {

    // 문제 생성
    QuestionResponse createQuestion(Long quizIdx, QuestionRequest request);

    // 문제 전체 조회
    List<QuestionResponse> getQuestionList(Long quizIdx);

    // 문제 단일 조회
    QuestionResponse getQuestion(Long questionIdx);

    // 문제 수정
    QuestionResponse patchQuestion(Long questionIdx, QuestionRequest request);

    // 문제 삭제
    void deleteQuestion(Long questionIdx);

    // 랜덤 문제 조회
    List<QuestionResponse> getRandomQuestionList(Long quizIdx);
}