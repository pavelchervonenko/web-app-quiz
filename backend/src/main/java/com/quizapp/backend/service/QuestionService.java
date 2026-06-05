package com.quizapp.backend.service;

import com.quizapp.backend.dto.quiz.QuestionCreateRequest;
import com.quizapp.backend.dto.quiz.QuestionUpdateRequest;
import com.quizapp.backend.dto.quiz.QuestionDTO;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface QuestionService {

    QuestionDTO addQuestion(UUID quizId, QuestionCreateRequest request, Authentication authentication);

    QuestionDTO updateQuestion(
        UUID quizId,
        UUID questionId,
        QuestionUpdateRequest request,
        Authentication authentication
    );

    void deleteQuestion(UUID quizId, UUID questionId, Authentication authentication);
}
