package kaguya.domain.answer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kaguya.domain.answer.model.entity.AnswerEntity;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {

    List<AnswerEntity> findByQuestionIdx(Long questionIdx);
}