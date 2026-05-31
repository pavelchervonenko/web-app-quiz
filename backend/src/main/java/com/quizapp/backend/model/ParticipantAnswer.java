package com.quizapp.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
    name = "participant_answers",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_participant_answers_participant_question",
            columnNames = {"participant_session_id", "question_id"}
        )
    },
    indexes = {
        @Index(name = "idx_participant_answers_quiz_session_id", columnList = "quiz_session_id"),
        @Index(name = "idx_participant_answers_participant_session_id", columnList = "participant_session_id"),
        @Index(name = "idx_participant_answers_question_id", columnList = "question_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class ParticipantAnswer extends BaseEntity {

    @NotNull
    @ToString.Include
    @Column(name = "is_correct", nullable = false)
    private Boolean correct;

    @NotNull
    @PositiveOrZero
    @ToString.Include
    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @NotNull
    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_session_id", nullable = false)
    private QuizSession quizSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_session_id", nullable = false)
    private ParticipantSession participantSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @OneToMany(
        mappedBy = "participantAnswer",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ParticipantAnswerOption> selectedOptions = new ArrayList<>();


    // ХЕЛПЕРЫ
    public void addSelectedOption(ParticipantAnswerOption selectedOption) {
        selectedOptions.add(selectedOption);
        selectedOption.setParticipantAnswer(this);
    }

    public void removeSelectedOption(ParticipantAnswerOption selectedOption) {
        selectedOptions.remove(selectedOption);
        selectedOption.setParticipantAnswer(null);
    }
}
