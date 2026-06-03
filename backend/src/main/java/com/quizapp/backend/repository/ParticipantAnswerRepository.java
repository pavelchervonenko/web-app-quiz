package com.quizapp.backend.repository;

import com.quizapp.backend.model.ParticipantAnswer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantAnswerRepository extends JpaRepository<ParticipantAnswer, UUID> {

    Optional<ParticipantAnswer> findByParticipantSessionIdAndQuestionId(UUID participantSessionId, UUID questionId);

    @EntityGraph(attributePaths = {"selectedOptions", "selectedOptions.answerOption"})
    Optional<ParticipantAnswer> findWithSelectedOptionsById(UUID id);

    List<ParticipantAnswer> findAllByQuizSessionIdAndParticipantSessionId(
        UUID quizSessionId,
        UUID participantSessionId
    );

    boolean existsByParticipantSessionIdAndQuestionId(UUID participantSessionId, UUID questionId);
}
