package com.quizapp.backend.dto.user;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record ParticipationHistoryDTO(
    UUID participantSessionId,
    UUID sessionId,
    UUID quizId,
    String quizTitle,
    String roomCode,
    QuizSessionStatus status,
    String displayName,
    Integer score,
    Instant joinedAt,
    Instant finishedAt
) {
}
