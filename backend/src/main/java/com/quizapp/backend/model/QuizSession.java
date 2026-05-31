package com.quizapp.backend.model;

import com.quizapp.backend.model.enums.QuizSessionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;

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
    name = "quiz_sessions",
    indexes = {
        @Index(name = "idx_quiz_sessions_room_code", columnList = "room_code", unique = true),
        @Index(name = "idx_quiz_sessions_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_quiz_sessions_organizer_id", columnList = "organizer_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class QuizSession extends BaseEntity {

    @NotBlank
    @ToString.Include
    @Column(name = "room_code", nullable = false, unique = true, length = 12)
    private String roomCode;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private QuizSessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_question_id")
    private Question currentQuestion;

    @Column(name = "current_question_started_at")
    private Instant currentQuestionStartedAt;

    @Column(name = "current_question_ends_at")
    private Instant currentQuestionEndsAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "quizSession")
    private List<ParticipantSession> participantSessions = new ArrayList<>();

    @OneToMany(mappedBy = "quizSession")
    private List<ParticipantAnswer> participantAnswers = new ArrayList<>();
}
