package com.quizapp.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ParticipantAnswerOptionUpdateDTO {

    private UUID participantAnswerId;

    private UUID answerOptionId;
}
