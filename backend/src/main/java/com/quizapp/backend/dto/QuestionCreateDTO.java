package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuestionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuestionCreateDTO {

    @NotNull
    private UUID quizId;

    @NotBlank
    private String text;

    private String imageUrl;

    @NotNull
    private QuestionType type;

    @NotNull
    @Positive
    private Integer timeLimitSeconds;

    @NotNull
    @PositiveOrZero
    private Integer pointsCorrect;

    @NotNull
    @PositiveOrZero
    private Integer pointsIncorrect;

    @NotNull
    @Positive
    private Integer position;
}
