package com.quizapp.backend.service;

import com.quizapp.backend.dto.quiz.QuizCreateRequest;
import com.quizapp.backend.dto.quiz.QuizDetailsDTO;
import com.quizapp.backend.dto.quiz.QuizSummaryDTO;
import com.quizapp.backend.dto.quiz.QuizUpdateRequest;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizDetailsDTO createQuiz(QuizCreateRequest request, Authentication authentication);

    List<QuizSummaryDTO> getMyQuizzes(Authentication authentication);

    QuizDetailsDTO getQuiz(UUID id, Authentication authentication);

    QuizDetailsDTO updateQuiz(UUID id, QuizUpdateRequest request, Authentication authentication);

    void deleteQuiz(UUID id, Authentication authentication);
}
