package com.quizapp.backend.model;

import com.quizapp.backend.model.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class User extends BaseEntity {

    @Email
    @NotBlank
    @ToString.Include
    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @ToString.Include
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // СВЯЗЬ
    @OneToMany(mappedBy = "owner")
    private List<Quiz> quizzes = new ArrayList<>();

    @OneToMany(mappedBy = "organizer")
    private List<QuizSession> organizedSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ParticipantSession> participantSessions = new ArrayList<>();
}
