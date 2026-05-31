package com.quizapp.backend.dto;

import com.quizapp.backend.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserPatchDTO {

    private JsonNullable<@Email @Size(max = 320) String> email = JsonNullable.undefined();

    private JsonNullable<@Size(min = 8, max = 100) String> password = JsonNullable.undefined();

    private JsonNullable<@Size(max = 100) String> displayName = JsonNullable.undefined();

    private JsonNullable<UserRole> role = JsonNullable.undefined();
}
