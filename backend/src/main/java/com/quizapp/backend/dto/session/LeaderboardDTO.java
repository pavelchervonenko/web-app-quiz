package com.quizapp.backend.dto.session;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.util.List;
import java.util.UUID;

public record LeaderboardDTO(
    UUID quizSessionId,
    QuizSessionStatus status,
    List<LeaderboardEntryDTO> entries
) {
}
