package kaguya.domain.question.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kaguya.domain.question.model.dto.request.QuestionRequest;
import kaguya.domain.question.model.dto.response.QuestionResponse;
import kaguya.domain.question.service.QuestionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // 문제 생성
    @PostMapping("/quizzes/{quizIdx}/questions")
    public ResponseEntity<QuestionResponse> createQuestion(
            @PathVariable("quizIdx") Long quizIdx,
            @RequestBody QuestionRequest request
    ) {
        QuestionResponse response = questionService.createQuestion(quizIdx, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 문제 조회
    @GetMapping("/quizzes/{quizIdx}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestionList(
            @PathVariable("quizIdx") Long quizIdx
    ) {
        List<QuestionResponse> response = questionService.getQuestionList(quizIdx);
        return ResponseEntity.ok(response);
    }

    // 문제 단건 조회
    @GetMapping("/questions/{questionIdx}")
    public ResponseEntity<QuestionResponse> getQuestion(
            @PathVariable("questionIdx") Long questionIdx
    ) {
        QuestionResponse response = questionService.getQuestion(questionIdx);
        return ResponseEntity.ok(response);
    }

    // 문제 수정
    @PatchMapping("/questions/{questionIdx}")
    public ResponseEntity<QuestionResponse> patchQuestion(
            @PathVariable("questionIdx") Long questionIdx,
            @RequestBody QuestionRequest request
    ) {
        QuestionResponse response = questionService.patchQuestion(questionIdx, request);
        return ResponseEntity.ok(response);
    }

    // 문제 삭제
    @DeleteMapping("/questions/{questionIdx}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable("questionIdx") Long questionIdx
    ) {
        questionService.deleteQuestion(questionIdx);
        return ResponseEntity.noContent().build();
    }
}