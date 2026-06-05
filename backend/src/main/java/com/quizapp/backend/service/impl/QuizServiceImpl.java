package com.quizapp.backend.service.impl;

import com.quizapp.backend.dto.quiz.QuizCreateRequest;
import com.quizapp.backend.dto.quiz.QuizDetailsDTO;
import com.quizapp.backend.dto.quiz.QuizSummaryDTO;
import com.quizapp.backend.dto.quiz.QuizUpdateRequest;
import com.quizapp.backend.mapper.QuizMapper;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuizStatus;
import com.quizapp.backend.repository.QuizRepository;
import com.quizapp.backend.repository.QuizSessionRepository;
import com.quizapp.backend.service.CurrentUserService;
import com.quizapp.backend.service.QuizService;
import com.quizapp.backend.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;
    private final CurrentUserService currentUserService;
    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Override
    @Transactional
    public QuizDetailsDTO createQuiz(QuizCreateRequest request, Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title().trim());
        quiz.setDescription(request.description());
        quiz.setStatus(QuizStatus.DRAFT);
        quiz.setOwner(currentUser);

        Quiz savedQuiz = quizRepository.save(quiz);

        return quizMapper.toDetailsDTO(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDTO> getMyQuizzes(Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        return quizRepository.findAllByOwnerIdOrderByCreatedAtDesc(currentUser.getId()).stream()
            .map(quizMapper::toSummaryDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailsDTO getQuiz(UUID id, Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = quizRepository.findWithQuestionsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + id + " not found"));

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to access this quiz");
        }

        return quizMapper.toDetailsDTO(quiz);
    }

    @Override
    @Transactional
    public QuizDetailsDTO updateQuiz(UUID id, QuizUpdateRequest request, Authentication authentication) {

        Quiz quiz = quizRepository.findWithQuestionsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + id + " not found"));

        User currentUser = currentUserService.getCurrentUser(authentication);

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to update this quiz");
        }

        quizMapper.updateEntityFromRequest(request, quiz);

        Quiz savedQuiz = quizRepository.save(quiz);

        return quizMapper.toDetailsDTO(savedQuiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID id, Authentication authentication) {

        Quiz quiz = quizRepository.findWithQuestionsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + id + " not found"));

        User currentUser = currentUserService.getCurrentUser(authentication);

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this quiz");
        }

        if (quizSessionRepository.existsByQuizId(id)) {
            quiz.setStatus(QuizStatus.ARCHIVED);
            return;
        }

        quizRepository.delete(quiz);
    }
}
