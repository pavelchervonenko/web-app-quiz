package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizStatus;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class QuizPatchDTO {

    private JsonNullable<@Size(max = 255) String> title = JsonNullable.undefined();

    private JsonNullable<String> description = JsonNullable.undefined();

    private JsonNullable<QuizStatus> status = JsonNullable.undefined();
}
