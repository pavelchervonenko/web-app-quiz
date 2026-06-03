package com.quizapp.backend.dto.session;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.util.UUID;

public record JoinQuizSessionResponse(
    UUID quizSessionId,
    UUID participantSessionId,
    String roomCode,
    String displayName,
    QuizSessionStatus status
) {
}
