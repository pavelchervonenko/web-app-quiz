package com.quizapp.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "participant_answer_options",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_participant_answer_options_answer_option",
            columnNames = {"participant_answer_id", "answer_option_id"}
        )
    },
    indexes = {
        @Index(name = "idx_participant_answer_options_answer_id", columnList = "participant_answer_id"),
        @Index(name = "idx_participant_answer_options_option_id", columnList = "answer_option_id")
    }
)
public class ParticipantAnswerOption extends BaseEntity {

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_answer_id", nullable = false)
    private ParticipantAnswer participantAnswer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_option_id", nullable = false)
    private AnswerOption answerOption;
}
