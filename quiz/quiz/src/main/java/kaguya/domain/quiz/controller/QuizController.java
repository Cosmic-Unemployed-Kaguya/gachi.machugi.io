package kaguya.domain.quiz.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kaguya.domain.quiz.model.dto.request.QuizCreateRequest;
import kaguya.domain.quiz.model.dto.request.QuizUpdateRequest;
import kaguya.domain.quiz.model.dto.response.QuizResponse;
import kaguya.domain.quiz.model.dto.response.QuizStartResponse;
import kaguya.domain.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;


@RestController
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

    // 퀴즈 조회 API
    @GetMapping("/{quizIdx}")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable("quizIdx") Long quizIdx) {
        QuizResponse response = quizService.getQuiz(quizIdx);
        return ResponseEntity.ok(response);
    }

    // 퀴즈 전체 조회 API
    @GetMapping
    public ResponseEntity<List<QuizResponse>> getQuizList() {
        List<QuizResponse> response = quizService.getQuizList();
        return ResponseEntity.ok(response);
    }

    // 퀴즈 수정 API
    @PatchMapping("/{quizIdx}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable("quizIdx") Long quizIdx,
            @RequestBody QuizUpdateRequest request
    ) {
        QuizResponse response = quizService.updateQuiz(quizIdx, request);
        return ResponseEntity.ok(response);
    }

    // 퀴즈 삭제 API
    @DeleteMapping("/{quizIdx}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("quizIdx") Long quizIdx) {
        quizService.deleteQuiz(quizIdx);
        return ResponseEntity.noContent().build();
    }

    // 퀴즈 시작 API
    @PostMapping("/{quizIdx}/start")
    public ResponseEntity<QuizStartResponse> startQuiz(@PathVariable("quizIdx") Long quizIdx) {
        return ResponseEntity.ok(quizService.startQuiz(quizIdx));
    }
}