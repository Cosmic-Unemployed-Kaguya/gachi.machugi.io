package kaguya.domain.quiz.question.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kaguya.domain.quiz.question.model.entity.QuestionEntity;

public interface QuestionRepository extends JpaRepository<QuestionEntity, Long> {

    List<QuestionEntity> findByQuizIdxOrderBySortOrderAsc(Long quizIdx);
}