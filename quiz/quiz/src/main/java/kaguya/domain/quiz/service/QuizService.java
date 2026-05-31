package kaguya.domain.quiz.service;

import java.util.List;

import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.request.QuizUpdateRequest;
import kaguya.domain.quiz.model.dto.response.QuizResponse;

public interface QuizService {

    // 퀴즈 생성 
    QuizResponse createQuiz(QuizCreateRequest request);

    // 퀴즈 조회
    QuizResponse getQuiz(Long quizIdx);

    // 퀴즈 전체 조회
    List<QuizResponse> getQuizList();

    // 퀴즈 수정
    QuizResponse updateQuiz(Long quizIdx, QuizUpdateRequest request);

    // 퀴즈 삭제
    void deleteQuiz(Long quizIdx);
}