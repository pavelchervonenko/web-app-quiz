package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuestionType;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateDTO {

    private String text;

    private String imageUrl;

    private QuestionType type;

    @Positive
    private Integer timeLimitSeconds;

    @PositiveOrZero
    private Integer pointsCorrect;

    @PositiveOrZero
    private Integer pointsIncorrect;

    @Positive
    private Integer position;
}
