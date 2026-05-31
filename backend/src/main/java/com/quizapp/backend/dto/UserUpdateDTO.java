package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {

    @Email
    @Size(max = 320)
    private String email;

    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 100)
    private String displayName;

    private UserRole role;
}
