package kaguya.domain.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kaguya.domain.question.model.entity.QuestionEntity;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    // 퀴즈에 속한 질문 리스트 조회
    List<QuestionEntity> findByQuizIdx(Long quizIdx);
} 