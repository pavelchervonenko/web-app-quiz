package com.quizapp.backend.dto.quiz;

import com.quizapp.backend.model.enums.QuestionType;

import java.util.List;
import java.util.UUID;

public record QuestionDTO(
    UUID id,
    String text,
    String imageUrl,
    QuestionType type,
    Integer timeLimitSeconds,
    Integer pointsCorrect,
    Integer pointsIncorrect,
    Integer position,
    List<AnswerOptionDTO> answerOptions
) {
}
