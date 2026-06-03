package com.quizapp.backend.dto.session;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import java.util.UUID;

public record SessionStateDTO(
    UUID quizSessionId,
    String roomCode,
    QuizSessionStatus status,
    CurrentQuestionDTO currentQuestion,
    LeaderboardDTO leaderboard
) {
}
