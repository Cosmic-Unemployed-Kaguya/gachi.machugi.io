package kaguya.quiz.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kaguya.quiz.domain.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import kaguya.quiz.domain.mapper.QuizMapper;
import kaguya.quiz.domain.model.dto.request.QuizCreateRequest;
import kaguya.quiz.domain.model.dto.request.QuizUpdateRequest;
import kaguya.quiz.domain.model.dto.response.QuizResponse;
import kaguya.quiz.domain.model.entity.QuizEntity;
import kaguya.quiz.domain.service.QuizService;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    // 퀴즈 생성
    @Override
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request) {

        QuizEntity quiz = quizMapper.requestToEntity(request);
        QuizEntity savedQuiz = quizRepository.save(quiz);

        return QuizResponse.from(savedQuiz);
    }

    // 퀴즈 조회
    @Override
    public QuizResponse getQuiz(Long quizIdx) {

        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        return QuizResponse.from(quiz);
    }

    // 퀴즈 전체 조회
    @Override
    public List<QuizResponse> getQuizList() {

        List<QuizEntity> quizzes = quizRepository.findAll();

        return quizzes.stream()
                .map(QuizResponse::from)
                .toList();
    }

    // 퀴즈 수정
    @Override
    public QuizResponse updateQuiz(Long quizIdx, QuizUpdateRequest request) {
        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        quizMapper.updateEntity(quiz, request);

        return QuizResponse.from(quiz);
    }

    // 퀴즈 삭제
    @Override
    public void deleteQuiz(Long quizIdx) {
        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        quizRepository.delete(quiz);
    }
}