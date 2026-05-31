package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class QuizSessionUpdateDTO {

    private QuizSessionStatus status;

    private UUID currentQuestionId;

    private Instant currentQuestionStartedAt;

    private Instant currentQuestionEndsAt;

    private Instant startedAt;

    private Instant finishedAt;
}
