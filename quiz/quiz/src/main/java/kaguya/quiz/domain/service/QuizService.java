package kaguya.quiz.domain.service;

import kaguya.quiz.domain.model.dto.request.QuizCreateRequest;
import kaguya.quiz.domain.model.dto.response.QuizResponse;

public interface QuizService {
    QuizResponse createQuiz(QuizCreateRequest request);
}