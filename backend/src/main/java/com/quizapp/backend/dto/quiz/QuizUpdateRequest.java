package com.quizapp.backend.dto.quiz;

import com.quizapp.backend.model.enums.QuizStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizUpdateRequest(
    @NotBlank
    @Size(max = 255)
    String title,

    String description,

    @NotNull
    QuizStatus status
) {
}
