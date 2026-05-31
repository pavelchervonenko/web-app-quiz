package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerOptionPatchDTO {

    private JsonNullable<UUID> participantAnswerId = JsonNullable.undefined();

    private JsonNullable<UUID> answerOptionId = JsonNullable.undefined();
}
