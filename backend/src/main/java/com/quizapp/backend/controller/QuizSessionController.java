package com.quizapp.backend.controller;

import com.quizapp.backend.dto.session.JoinQuizSessionRequest;
import com.quizapp.backend.dto.session.JoinQuizSessionResponse;
import com.quizapp.backend.dto.session.QuizSessionDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.dto.session.SubmitAnswerRequest;
import com.quizapp.backend.dto.session.SubmittedAnswerDTO;
import com.quizapp.backend.service.QuizSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class QuizSessionController {

    private final QuizSessionService quizSessionService;

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PostMapping("/api/quizzes/{quizId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizSessionDTO startSession(
        @PathVariable UUID quizId,
        Authentication authentication
    ) {
        return quizSessionService.startSession(quizId, authentication);
    }

    @GetMapping("/api/sessions/{roomCode}/state")
    public SessionStateDTO getSessionState(
        @PathVariable String roomCode,
        Authentication authentication
    ) {
        return quizSessionService.getSessionState(roomCode, authentication);
    }

    @PostMapping("/api/sessions/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinQuizSessionResponse joinSession(
        @RequestBody @Valid JoinQuizSessionRequest request,
        Authentication authentication
    ) {
        return quizSessionService.joinSession(request, authentication);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PostMapping("/api/sessions/{sessionId}/questions/next")
    public SessionStateDTO showNextQuestion(
        @PathVariable UUID sessionId,
        Authentication authentication
    ) {
        return quizSessionService.showNextQuestion(sessionId, authentication);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PostMapping("/api/sessions/{sessionId}/questions/current/close")
    public SessionStateDTO closeCurrentQuestion(
        @PathVariable UUID sessionId,
        Authentication authentication
    ) {
        return quizSessionService.closeCurrentQuestion(sessionId, authentication);
    }

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PostMapping("/api/sessions/{sessionId}/finish")
    public SessionStateDTO finishSession(
        @PathVariable UUID sessionId,
        Authentication authentication
    ) {
        return quizSessionService.finishSession(sessionId, authentication);
    }

    @PostMapping("/api/sessions/{sessionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmittedAnswerDTO submitAnswer(
        @PathVariable UUID sessionId,
        @RequestBody @Valid SubmitAnswerRequest request,
        Authentication authentication
    ) {
        return quizSessionService.submitAnswer(sessionId, request, authentication);
    }
}
