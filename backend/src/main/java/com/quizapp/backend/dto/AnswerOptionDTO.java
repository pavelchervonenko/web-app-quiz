package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AnswerOptionDTO {

    private UUID id;

    private UUID questionId;

    private String text;

    private Boolean correct;

    private Integer position;

    private Instant updatedAt;

    private Instant createdAt;
}
