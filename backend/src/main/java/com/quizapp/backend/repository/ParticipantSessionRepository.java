package com.quizapp.backend.repository;

import com.quizapp.backend.model.ParticipantSession;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantSessionRepository extends JpaRepository<ParticipantSession, UUID> {

    Optional<ParticipantSession> findByQuizSessionIdAndDisplayName(UUID quizSessionId, String displayName);

    List<ParticipantSession> findAllByQuizSessionIdOrderByScoreDescJoinedAtAsc(UUID quizSessionId);

    @EntityGraph(attributePaths = {"quizSession", "quizSession.quiz"})
    List<ParticipantSession> findAllByUserIdOrderByJoinedAtDesc(UUID userId);

    boolean existsByQuizSessionIdAndDisplayName(UUID quizSessionId, String displayName);
}
