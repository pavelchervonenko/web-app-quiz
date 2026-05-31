package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuestionType;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class QuestionPatchDTO {

    private JsonNullable<String> text = JsonNullable.undefined();

    private JsonNullable<String> imageUrl = JsonNullable.undefined();

    private JsonNullable<QuestionType> type = JsonNullable.undefined();

    private JsonNullable<@Positive Integer> timeLimitSeconds = JsonNullable.undefined();

    private JsonNullable<@PositiveOrZero Integer> pointsCorrect = JsonNullable.undefined();

    private JsonNullable<@PositiveOrZero Integer> pointsIncorrect = JsonNullable.undefined();

    private JsonNullable<@Positive Integer> position = JsonNullable.undefined();
}
