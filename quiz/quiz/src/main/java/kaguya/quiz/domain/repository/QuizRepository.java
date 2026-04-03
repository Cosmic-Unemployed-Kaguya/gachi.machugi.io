package kaguya.quiz.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kaguya.quiz.domain.model.entity.QuizEntity;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    
}
