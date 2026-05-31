package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerOptionDTO {

    private UUID id;

    private UUID participantAnswerId;

    private UUID answerOptionId;

    private Instant updatedAt;

    private Instant createdAt;
}
