package com.quizapp.backend.controller;

import com.quizapp.backend.dto.quiz.QuestionCreateRequest;
import com.quizapp.backend.dto.quiz.QuestionDTO;
import com.quizapp.backend.dto.quiz.QuestionUpdateRequest;
import com.quizapp.backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDTO addQuestion(
        @PathVariable UUID quizId,
        @RequestBody @Valid  QuestionCreateRequest request,
        Authentication authentication
    ) {
        return questionService.addQuestion(quizId, request, authentication);
    }

    @PutMapping("/{questionId}")
    @ResponseStatus(HttpStatus.OK)
    public QuestionDTO updateQuestion(
        @PathVariable UUID quizId,
        @PathVariable  UUID questionId,
        @RequestBody @Valid QuestionUpdateRequest request,
        Authentication authentication
    ) {
        return questionService.updateQuestion(quizId, questionId, request, authentication);
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(
        @PathVariable UUID quizId,
        @PathVariable UUID questionId,
        Authentication authentication
    ) {
        questionService.deleteQuestion(quizId, questionId, authentication);
    }
}
