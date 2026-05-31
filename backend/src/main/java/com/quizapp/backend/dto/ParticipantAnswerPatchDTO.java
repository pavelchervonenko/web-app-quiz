package com.quizapp.backend.dto;

import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerPatchDTO {

    private JsonNullable<Boolean> correct = JsonNullable.undefined();

    private JsonNullable<@PositiveOrZero Integer> pointsAwarded = JsonNullable.undefined();

    private JsonNullable<List<UUID>> selectedAnswerOptionIds = JsonNullable.undefined();
}
