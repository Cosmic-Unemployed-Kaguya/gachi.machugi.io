package kaguya.domain.answer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kaguya.domain.answer.model.dto.request.AnswerRequest;
import kaguya.domain.answer.model.dto.response.AnswerResponse;
import kaguya.domain.answer.model.dto.request.AnswerCheckRequest;
import kaguya.domain.answer.model.dto.response.AnswerCheckResponse;
import kaguya.domain.answer.service.AnswerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    // 정답 생성
    @PostMapping("/questions/{questionIdx}/answers")
    public ResponseEntity<AnswerResponse> createAnswer(
            @PathVariable("questionIdx") Long questionIdx,
            @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = answerService.createAnswer(questionIdx, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 문제별 전체 정답 조회
    @GetMapping("/questions/{questionIdx}/answers")
    public ResponseEntity<List<AnswerResponse>> getAnswerList(
            @PathVariable("questionIdx") Long questionIdx
    ) {
        List<AnswerResponse> response = answerService.getAnswerList(questionIdx);
        return ResponseEntity.ok(response);
    }

    // 정답 수정
    @PatchMapping("/answers/{answerIdx}")
    public ResponseEntity<AnswerResponse> patchAnswer(
            @PathVariable("answerIdx") Long answerIdx,
            @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = answerService.patchAnswer(answerIdx, request);
        return ResponseEntity.ok(response);
    }

    // 정답 삭제
    @DeleteMapping("/answers/{answerIdx}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable("answerIdx") Long answerIdx
    ) {
        answerService.deleteAnswer(answerIdx);
        return ResponseEntity.noContent().build();
    }

    // 정답 확인
    @PostMapping("/questions/{questionIdx}/answers/check")
    public ResponseEntity<AnswerCheckResponse> checkAnswer(
            @PathVariable("questionIdx") Long questionIdx,
            @RequestBody AnswerCheckRequest request
    ) {
        AnswerCheckResponse response = answerService.checkAnswer(
                questionIdx, 
                request
        );
        
        return ResponseEntity.ok(response);
    }
}