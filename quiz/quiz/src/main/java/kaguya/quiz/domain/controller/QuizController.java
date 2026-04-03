package kaguya.quiz.domain.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import kaguya.quiz.domain.service.QuizService;
import kaguya.quiz.domain.model.dto.request.QuizCreateRequest;
import kaguya.quiz.domain.model.dto.response.QuizResponse;
import lombok.RequiredArgsConstructor;

@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 생성 API
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(@RequestBody QuizCreateRequest request) {
        QuizResponse response = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}