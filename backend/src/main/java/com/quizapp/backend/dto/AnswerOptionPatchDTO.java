package com.quizapp.backend.dto;

import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class AnswerOptionPatchDTO {

    private JsonNullable<String> text = JsonNullable.undefined();

    private JsonNullable<Boolean> correct = JsonNullable.undefined();

    private JsonNullable<@Positive Integer> position = JsonNullable.undefined();
}
