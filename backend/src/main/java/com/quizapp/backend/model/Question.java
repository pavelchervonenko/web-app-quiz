package com.quizapp.backend.model;

import com.quizapp.backend.model.enums.QuestionType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

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
    name = "questions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_questions_quiz_position", columnNames = {"quiz_id", "position"})
    },
    indexes = {
        @Index(name = "idx_questions_quiz_id", columnList = "quiz_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class Question extends BaseEntity {

    @NotBlank
    @ToString.Include
    @Column(columnDefinition = "text")
    private String text;

    @Column(name = "image_url")
    private String imageUrl;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @NotNull
    @Positive
    @Column(name = "time_limit_seconds", nullable = false)
    private Integer timeLimitSeconds;

    @NotNull
    @PositiveOrZero
    @Column(name = "points_correct", nullable = false)
    private Integer pointsCorrect;

    @NotNull
    @PositiveOrZero
    @Column(name = "points_incorrect", nullable = false)
    private Integer pointsIncorrect;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer position;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(
        mappedBy = "question",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("position ASC")
    private List<AnswerOption> answerOptions = new ArrayList<>();

    @OneToMany(mappedBy = "question")
    private List<ParticipantAnswer> participantAnswers = new ArrayList<>();

    // ХЕЛПЕРЫ
    public void addAnswer(AnswerOption answerOption) {
        answerOptions.add(answerOption);
        answerOption.setQuestion(this);
    }

    public void removeAnswerOption(AnswerOption answerOption) {
        answerOptions.remove(answerOption);
        answerOption.setQuestion(null);
    }
}
