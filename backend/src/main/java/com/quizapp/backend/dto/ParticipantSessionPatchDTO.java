package com.quizapp.backend.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;

@Getter
@Setter
public class ParticipantSessionPatchDTO {

    private JsonNullable<@Size(max = 100) String> displayName = JsonNullable.undefined();

    private JsonNullable<@PositiveOrZero Integer> score = JsonNullable.undefined();

    private JsonNullable<Instant> finishedAt = JsonNullable.undefined();
}
