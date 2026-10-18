package kaguya.domain.question.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kaguya.domain.question.model.dto.request.QuestionRequest;
import kaguya.domain.question.model.dto.response.QuestionResponse;
import kaguya.domain.question.model.entity.QuestionEntity;
import kaguya.domain.question.repository.QuestionRepository;
import kaguya.domain.question.service.QuestionService;
import kaguya.domain.question.util.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    // 문제 생성
    @Override
    public QuestionResponse createQuestion(Long quizIdx, QuestionRequest request) {
        QuestionEntity question = questionMapper.requestToEntity(quizIdx, request);
        QuestionEntity savedQuestion = questionRepository.save(question);

        return questionMapper.entityToResponse(savedQuestion);
    }

    // 문제 전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionList(Long quizIdx) {
        return questionRepository.findByQuizIdxOrderBySortOrderAsc(quizIdx)
                .stream()
                .map(questionMapper::entityToResponse)
                .toList();
    }

    // 문제 단일 조회 
    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestion(Long questionIdx) {
        QuestionEntity question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new RuntimeException("문제가 존재하지 않습니다."));

        return questionMapper.entityToResponse(question);
    }

    // 문제 수정
    @Override
    public QuestionResponse patchQuestion(Long questionIdx, QuestionRequest request) {
        QuestionEntity question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new RuntimeException("문제가 존재하지 않습니다."));

        question.updateQuestion(
                request.problemText(),
                request.problemUrl(),
                request.type(),
                request.sortOrder()
        );

        return questionMapper.entityToResponse(question);
    }

    // 문제 삭제
    @Override
    public void deleteQuestion(Long questionIdx) {
        QuestionEntity question = questionRepository.findById(questionIdx)
                .orElseThrow(() -> new RuntimeException("문제가 존재하지 않습니다."));

        questionRepository.delete(question);
    }
}