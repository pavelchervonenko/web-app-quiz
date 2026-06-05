package com.quizapp.backend.mapper;

import com.quizapp.backend.dto.session.QuizSessionDTO;
import com.quizapp.backend.model.QuizSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuizSessionMapper {

    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "organizerId", source = "organizer.id")
    @Mapping(target = "currentQuestionId", source = "currentQuestion.id")
    QuizSessionDTO toQuizSessionDTO(QuizSession quizSession);
}
