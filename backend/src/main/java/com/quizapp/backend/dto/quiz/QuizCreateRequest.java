package com.quizapp.backend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuizCreateRequest(
    @NotBlank
    @Size(max = 255)
    String title,

    String description
) {
}
