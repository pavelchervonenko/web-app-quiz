package com.quizapp.backend.dto.session;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SubmitAnswerRequest(
    @NotNull
    UUID participantSessionId,

    @NotNull
    UUID questionId,

    @NotEmpty
    List<UUID> selectedAnswerOptionIds
) {
}
