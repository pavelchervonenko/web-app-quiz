package com.quizapp.backend.model;

import com.quizapp.backend.model.enums.QuizStatus;

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
    name = "quizzes",
    indexes = {
        @Index(name = "idx_quizzes_owner_id", columnList = "owner_id")
    }
)
@ToString(onlyExplicitlyIncluded = true)
public class Quiz extends BaseEntity {

    @NotBlank
    @ToString.Include
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    @ToString.Include
    private String description;

    @ToString.Include
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status;

    // СВЯЗЬ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(
        mappedBy = "quiz",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<QuizSession> sessions = new ArrayList<>();

    // ХЕЛПЕРЫ
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }
}
