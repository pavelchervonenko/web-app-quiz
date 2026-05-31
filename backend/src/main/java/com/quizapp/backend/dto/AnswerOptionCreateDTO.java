package com.quizapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AnswerOptionCreateDTO {

    @NotNull
    private UUID questionId;

    @NotBlank
    private String text;

    @NotNull
    private Boolean correct;

    @NotNull
    @Positive
    private Integer position;
}
