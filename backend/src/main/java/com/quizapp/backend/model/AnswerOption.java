package com.quizapp.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "answer_options",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_answer_options_question_position", columnNames = {"question_id", "position"})
    },
    indexes = {
        @Index(name = "idx_answer_options_question_id", columnList = "question_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class AnswerOption extends BaseEntity {

    @NotBlank
    @ToString.Include
    @Column(columnDefinition = "text")
    private String text;

    @NotNull
    @ToString.Include
    @Column(name = "is_correct", nullable = false)
    private Boolean correct;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer position;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
