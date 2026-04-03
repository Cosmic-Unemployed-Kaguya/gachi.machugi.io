package kaguya.quiz.domain.service.impl;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kaguya.quiz.domain.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import kaguya.quiz.domain.mapper.QuizMapper;
import kaguya.quiz.domain.model.dto.request.QuizCreateRequest;
import kaguya.quiz.domain.model.dto.response.QuizResponse;
import kaguya.quiz.domain.model.entity.QuizEntity;
import kaguya.quiz.domain.service.QuizService;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    @Override
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request) {

        QuizEntity quiz = quizMapper.requestToEntity(request);
        QuizEntity savedQuiz = quizRepository.save(quiz);

        return QuizResponse.from(savedQuiz);
    }
}