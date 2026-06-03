package com.quizapp.backend.dto.quiz;

import com.quizapp.backend.model.enums.QuizStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizDetailsDTO(
    UUID id,
    UUID ownerId,
    String title,
    String description,
    QuizStatus status,
    List<QuestionDTO> questions,
    Instant createdAt,
    Instant updatedAt
) {
}
