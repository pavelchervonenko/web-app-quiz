package com.quizapp.backend.dto.session;

import com.quizapp.backend.model.enums.QuestionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CurrentQuestionDTO(
    UUID id,
    String text,
    String imageUrl,
    QuestionType type,
    Integer position,
    Integer timeLimitSeconds,
    Instant startedAt,
    Instant endsAt,
    List<CurrentQuestionOptionDTO> options
) {
}
