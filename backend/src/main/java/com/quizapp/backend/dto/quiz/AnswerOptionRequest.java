package com.quizapp.backend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnswerOptionRequest(
    @NotBlank
    String text,

    @NotNull
    Boolean correct,

    @NotNull
    @Positive
    Integer position
) {
}
