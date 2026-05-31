package com.quizapp.backend.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuizSessionCreateDTO {

    @NotNull
    private UUID quizId;

    @NotNull
    private UUID organizerId;
}
