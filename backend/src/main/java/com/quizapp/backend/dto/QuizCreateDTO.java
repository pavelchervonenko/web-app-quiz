package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class QuizCreateDTO {

    @NotNull
    private UUID ownerId;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    private QuizStatus status;
}
