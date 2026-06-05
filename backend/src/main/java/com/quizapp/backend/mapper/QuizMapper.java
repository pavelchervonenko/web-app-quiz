package com.quizapp.backend.mapper;

import com.quizapp.backend.dto.quiz.QuizDetailsDTO;
import com.quizapp.backend.dto.quiz.QuizSummaryDTO;
import com.quizapp.backend.dto.quiz.QuizUpdateRequest;
import com.quizapp.backend.model.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = QuestionMapper.class
)
public interface QuizMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    QuizDetailsDTO toDetailsDTO(Quiz quiz);

    @Mapping(target = "questionCount", expression = "java(getQuestionCount(quiz))")
    QuizSummaryDTO toSummaryDTO(Quiz quiz);

    void updateEntityFromRequest(QuizUpdateRequest request, @MappingTarget Quiz quiz);

    default Integer getQuestionCount(Quiz quiz) {
        return quiz.getQuestions() == null ? 0 : quiz.getQuestions().size();
    }
}
