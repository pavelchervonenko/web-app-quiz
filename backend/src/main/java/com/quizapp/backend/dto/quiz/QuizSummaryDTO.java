package com.quizapp.backend.dto.quiz;

import com.quizapp.backend.model.enums.QuizStatus;

import java.time.Instant;
import java.util.UUID;

public record QuizSummaryDTO(
    UUID id,
    String title,
    String description,
    QuizStatus status,
    Integer questionCount,
    Instant createdAt,
    Instant updatedAt
) {
}
