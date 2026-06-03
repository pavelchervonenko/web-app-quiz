package com.quizapp.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
    @Email
    @Size(max = 320)
    String email,

    @Size(max = 100)
    String displayName
) {
}
