package com.quizapp.backend.repository;

import com.quizapp.backend.model.Question;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findAllByQuizIdOrderByPositionAsc(UUID quizId);

    @EntityGraph(attributePaths = "answerOptions")
    Optional<Question> findWithAnswerOptionsById(UUID id);

    Optional<Question> findByQuizIdAndPosition(UUID quizId, Integer position);

    boolean existsByQuizIdAndPosition(UUID quizId, Integer position);
}
