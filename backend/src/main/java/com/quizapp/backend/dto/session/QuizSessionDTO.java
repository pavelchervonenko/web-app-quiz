package com.quizapp.backend.dto.session;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record QuizSessionDTO(
    UUID id,
    UUID quizId,
    UUID organizerId,
    String roomCode,
    QuizSessionStatus status,
    UUID currentQuestionId,
    Instant currentQuestionStartedAt,
    Instant currentQuestionEndsAt,
    Instant startedAt,
    Instant finishedAt,
    Instant createdAt,
    Instant updatedAt
) {
}
