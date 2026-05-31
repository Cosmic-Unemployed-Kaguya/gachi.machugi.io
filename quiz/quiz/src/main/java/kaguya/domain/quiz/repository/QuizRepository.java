package kaguya.domain.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kaguya.domain.quiz.model.entity.QuizEntity;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    
}
