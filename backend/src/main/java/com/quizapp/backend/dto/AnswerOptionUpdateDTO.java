package com.quizapp.backend.dto;

import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOptionUpdateDTO {

    private String text;

    private Boolean correct;

    @Positive
    private Integer position;
}
