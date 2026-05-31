package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizStatus;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizUpdateDTO {

    @Size(max = 255)
    private String title;

    private String description;

    private QuizStatus status;
}
