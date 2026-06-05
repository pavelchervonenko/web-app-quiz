package com.quizapp.backend.controller;

import com.quizapp.backend.dto.quiz.QuizCreateRequest;
import com.quizapp.backend.dto.quiz.QuizDetailsDTO;
import com.quizapp.backend.dto.quiz.QuizSummaryDTO;
import com.quizapp.backend.dto.quiz.QuizUpdateRequest;
import com.quizapp.backend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizDetailsDTO createQuiz(@RequestBody @Valid QuizCreateRequest request, Authentication authentication) {
        return quizService.createQuiz(request, authentication);
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public List<QuizSummaryDTO> getMyQuizzes(Authentication authentication) {
        return quizService.getMyQuizzes(authentication);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QuizDetailsDTO getQuiz(@PathVariable UUID id, Authentication authentication) {
        return quizService.getQuiz(id, authentication);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QuizDetailsDTO updateQuiz(
        @PathVariable UUID id,
        @RequestBody @Valid QuizUpdateRequest request,
        Authentication authentication
    ) {
        return quizService.updateQuiz(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable UUID id, Authentication authentication) {
        quizService.deleteQuiz(id, authentication);
    }
}
