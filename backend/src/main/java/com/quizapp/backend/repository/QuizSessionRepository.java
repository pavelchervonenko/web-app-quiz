package com.quizapp.backend.repository;

import com.quizapp.backend.model.QuizSession;
import com.quizapp.backend.model.enums.QuizSessionStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {

    Optional<QuizSession> findByRoomCode(String roomCode);

    @EntityGraph(attributePaths = {"quiz", "currentQuestion", "currentQuestion.answerOptions"})
    Optional<QuizSession> findWithStateByRoomCode(String roomCode);

    @EntityGraph(attributePaths = {"quiz", "participantSessions"})
    List<QuizSession> findAllByOrganizerIdOrderByCreatedAtDesc(UUID organizerId);

    List<QuizSession> findAllByQuizIdOrderByCreatedAtDesc(UUID quizId);

    boolean existsByRoomCode(String roomCode);

    boolean existsByIdAndOrganizerId(UUID id, UUID organizerId);

    boolean existsByQuizId(UUID quizId);

    boolean existsByQuizIdAndStatusIn(UUID quizId, Collection<QuizSessionStatus> statuses);

    long countByQuizIdAndStatusNot(UUID quizId, QuizSessionStatus status);
}
