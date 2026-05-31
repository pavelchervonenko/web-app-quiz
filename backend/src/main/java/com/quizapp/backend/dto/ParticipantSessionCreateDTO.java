package com.quizapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ParticipantSessionCreateDTO {

    @NotNull
    private UUID quizSessionId;

    private UUID userId;

    @NotBlank
    @Size(max = 100)
    private String displayName;
}
