package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.UserRole;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UserDTO {

    private UUID id;

    private String email;

    private String displayName;

    private UserRole role;

    private Instant updatedAt;

    private Instant createdAt;
}
