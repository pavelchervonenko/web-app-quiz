package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ParticipantSessionDTO {

    private UUID id;

    private UUID quizSessionId;

    private UUID userId;

    private String displayName;

    private Integer score;

    private Instant joinedAt;

    private Instant finishedAt;

    private Instant updatedAt;

    private Instant createdAt;
}
