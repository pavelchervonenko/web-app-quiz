package com.quizapp.backend.dto.user;

import com.quizapp.backend.model.enums.UserRole;

import java.util.UUID;

public record CurrentUserDTO(
    UUID id,
    String email,
    String displayName,
    UserRole role
) {
}
