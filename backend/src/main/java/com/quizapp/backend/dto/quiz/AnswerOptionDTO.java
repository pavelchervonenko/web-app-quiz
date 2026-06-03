package com.quizapp.backend.dto.quiz;

import java.util.UUID;

public record AnswerOptionDTO(
    UUID id,
    String text,
    Boolean correct,
    Integer position
) {
}
