package com.quizapp.backend.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ParticipantSessionUpdateDTO {

    @Size(max = 100)
    private String displayName;

    @PositiveOrZero
    private Integer score;

    private Instant finishedAt;
}
