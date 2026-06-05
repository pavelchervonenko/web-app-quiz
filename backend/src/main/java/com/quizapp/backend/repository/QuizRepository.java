package com.quizapp.backend.repository;

import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.enums.QuizStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    List<Quiz> findAllByOwnerIdAndStatusOrderByCreatedAtDesc(UUID ownerId, QuizStatus status);

    @EntityGraph(attributePaths = "questions")
    Optional<Quiz> findWithQuestionsById(UUID id);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
}
