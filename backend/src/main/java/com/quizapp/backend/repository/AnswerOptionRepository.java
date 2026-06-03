package com.quizapp.backend.repository;

import com.quizapp.backend.model.AnswerOption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {

    List<AnswerOption> findAllByQuestionIdOrderByPositionAsc(UUID questionId);

    List<AnswerOption> findAllByIdIn(List<UUID> ids);

    boolean existsByQuestionIdAndPosition(UUID questionId, Integer position);
}
