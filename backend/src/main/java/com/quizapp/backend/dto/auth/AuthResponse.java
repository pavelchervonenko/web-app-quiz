package com.quizapp.backend.dto.auth;

import com.quizapp.backend.dto.user.CurrentUserDTO;

public record AuthResponse(
    String accessToken,
    String tokenType,
    CurrentUserDTO user
) {
}
