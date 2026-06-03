package com.quizapp.backend.repository;

import com.quizapp.backend.model.ParticipantAnswerOption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParticipantAnswerOptionRepository extends JpaRepository<ParticipantAnswerOption, UUID> {

    List<ParticipantAnswerOption> findAllByParticipantAnswerId(UUID participantAnswerId);
}
