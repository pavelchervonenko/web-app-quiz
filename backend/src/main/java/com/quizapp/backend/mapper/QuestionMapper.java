package com.quizapp.backend.mapper;

import com.quizapp.backend.dto.quiz.QuestionDTO;
import com.quizapp.backend.dto.quiz.QuestionUpdateRequest;
import com.quizapp.backend.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface QuestionMapper {

    QuestionDTO toQuestionDTO(Question question);

    @Mapping(target = "answerOptions", ignore = true)
    void updateEntityFromRequest(QuestionUpdateRequest request, @MappingTarget Question question);
}
