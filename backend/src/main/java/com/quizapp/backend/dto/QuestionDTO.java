package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuestionType;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class QuestionDTO {

    private UUID id;

    private UUID quizId;

    private String text;

    private String imageUrl;

    private QuestionType type;

    private Integer timeLimitSeconds;

    private Integer pointsCorrect;

    private Integer pointsIncorrect;

    private Integer position;

    private Instant updatedAt;

    private Instant createdAt;
}
