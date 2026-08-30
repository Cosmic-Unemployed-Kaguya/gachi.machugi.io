package kaguya.domain.quiz.service;

import java.util.List;

import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.request.QuizUpdateRequest;
import kaguya.domain.quiz.model.dto.response.QuizResponse;
import kaguya.domain.quiz.model.dto.response.QuizStartResponse;

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

    // 퀴즈 시작 ( 해당 퀴즈에 대한 문제들 랜덤으로 불러오기 )
    QuizStartResponse startQuiz(Long quizIdx);
}