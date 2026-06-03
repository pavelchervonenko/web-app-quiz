package com.quizapp.backend.dto.quiz;

import com.quizapp.backend.model.enums.QuestionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record QuestionCreateRequest(
    @NotBlank
    String text,

    String imageUrl,

    @NotNull
    QuestionType type,

    @NotNull
    @Positive
    Integer timeLimitSeconds,

    @NotNull
    @PositiveOrZero
    Integer pointsCorrect,

    @NotNull
    @PositiveOrZero
    Integer pointsIncorrect,

    @NotNull
    @Positive
    Integer position,

    @Valid
    @NotEmpty
    List<AnswerOptionRequest> answerOptions
) {
}
