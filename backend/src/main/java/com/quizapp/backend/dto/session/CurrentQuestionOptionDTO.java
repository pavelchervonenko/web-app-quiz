package com.quizapp.backend.dto.session;

import java.util.UUID;

public record CurrentQuestionOptionDTO(
    UUID id,
    String text,
    Integer position
) {
}
