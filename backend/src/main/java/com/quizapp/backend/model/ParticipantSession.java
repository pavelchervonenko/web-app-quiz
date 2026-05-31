package com.quizapp.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "participant_sessions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_participant_sessions_session_display_name",
            columnNames = {"quiz_session_id", "display_name"}
        )
    },
    indexes = {
        @Index(name = "idx_participant_sessions_quiz_session_id", columnList = "quiz_session_id"),
        @Index(name = "idx_participant_sessions_user_id", columnList = "user_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class ParticipantSession extends BaseEntity {

    @NotBlank
    @ToString.Include
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @NotNull
    @PositiveOrZero
    @ToString.Include
    @Column(nullable = false)
    private Integer score = 0;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_session_id", nullable = false)
    private QuizSession quizSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "participantSession")
    private List<ParticipantAnswer> answers = new ArrayList<>();
}
