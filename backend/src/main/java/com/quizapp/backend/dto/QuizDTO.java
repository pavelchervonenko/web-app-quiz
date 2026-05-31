package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.QuizStatus;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class QuizDTO {

    private UUID id;

    private UUID ownerId;

    private String title;

    private String description;

    private QuizStatus status;

    private Instant updatedAt;

    private Instant createdAt;
}
