package com.quizapp.backend.service;

import com.quizapp.backend.dto.session.JoinQuizSessionRequest;
import com.quizapp.backend.dto.session.JoinQuizSessionResponse;
import com.quizapp.backend.dto.session.QuizSessionDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.dto.session.SubmitAnswerRequest;
import com.quizapp.backend.dto.session.SubmittedAnswerDTO;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface QuizSessionService {

    QuizSessionDTO startSession(UUID quizId, Authentication authentication);

    SessionStateDTO getSessionState(String roomCode, Authentication authentication);

    JoinQuizSessionResponse joinSession(JoinQuizSessionRequest request, Authentication authentication);

    SessionStateDTO showNextQuestion(UUID sessionId, Authentication authentication);

    SessionStateDTO closeCurrentQuestion(UUID sessionId, Authentication authentication);

    SessionStateDTO finishSession(UUID sessionId, Authentication authentication);

    SubmittedAnswerDTO submitAnswer(UUID sessionId, SubmitAnswerRequest request, Authentication authentication);
}
