package com.quizapp.backend.dto;

import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantAnswerUpdateDTO {

    private Boolean correct;

    @PositiveOrZero
    private Integer pointsAwarded;
}
