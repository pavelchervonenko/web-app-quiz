package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerDTO {

    private UUID id;

    private UUID quizSessionId;

    private UUID participantSessionId;

    private UUID questionId;

    private Boolean correct;

    private Integer pointsAwarded;

    private Instant answeredAt;

    private List<UUID> selectedAnswerOptionIds;

    private Instant updatedAt;

    private Instant createdAt;
}
