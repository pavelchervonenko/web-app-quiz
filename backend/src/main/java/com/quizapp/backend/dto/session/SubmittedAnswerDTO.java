package com.quizapp.backend.dto.session;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubmittedAnswerDTO(
    UUID id,
    UUID participantSessionId,
    UUID questionId,
    List<UUID> selectedAnswerOptionIds,
    Boolean correct,
    Integer pointsAwarded,
    Integer totalScore,
    Instant answeredAt
) {
}
