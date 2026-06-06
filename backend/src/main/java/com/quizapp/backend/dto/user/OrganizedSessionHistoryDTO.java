package com.quizapp.backend.dto.user;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record OrganizedSessionHistoryDTO(
    UUID sessionId,
    UUID quizId,
    String quizTitle,
    String roomCode,
    QuizSessionStatus status,
    Integer participantCount,
    Instant startedAt,
    Instant finishedAt,
    Instant createdAt
) {
}
