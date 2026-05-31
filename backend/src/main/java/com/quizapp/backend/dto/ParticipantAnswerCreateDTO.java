package com.quizapp.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerCreateDTO {

    @NotNull
    private UUID quizSessionId;

    @NotNull
    private UUID participantSessionId;

    @NotNull
    private UUID questionId;

    @NotEmpty
    private List<UUID> selectedAnswerOptionIds;
}
