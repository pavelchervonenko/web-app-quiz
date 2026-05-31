package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class QuizSessionPatchDTO {

    private JsonNullable<QuizSessionStatus> status = JsonNullable.undefined();

    private JsonNullable<UUID> currentQuestionId = JsonNullable.undefined();

    private JsonNullable<Instant> currentQuestionStartedAt = JsonNullable.undefined();

    private JsonNullable<Instant> currentQuestionEndsAt = JsonNullable.undefined();

    private JsonNullable<Instant> startedAt = JsonNullable.undefined();

    private JsonNullable<Instant> finishedAt = JsonNullable.undefined();
}
