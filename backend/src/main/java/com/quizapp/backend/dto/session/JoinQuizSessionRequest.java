package com.quizapp.backend.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinQuizSessionRequest(
    @NotBlank
    @Size(max = 12)
    String roomCode,

    @NotBlank
    @Size(max = 100)
    String displayName
) {
}
