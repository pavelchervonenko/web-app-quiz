package com.quizapp.backend.service.impl;

import com.quizapp.backend.dto.quiz.QuestionCreateRequest;
import com.quizapp.backend.dto.quiz.QuestionDTO;
import com.quizapp.backend.dto.quiz.QuestionUpdateRequest;
import com.quizapp.backend.dto.quiz.AnswerOptionRequest;
import com.quizapp.backend.exception.BadRequestException;
import com.quizapp.backend.exception.ConflictException;
import com.quizapp.backend.exception.ResourceNotFoundException;
import com.quizapp.backend.mapper.QuestionMapper;
import com.quizapp.backend.model.AnswerOption;
import com.quizapp.backend.model.Question;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuestionType;
import com.quizapp.backend.model.enums.QuizStatus;
import com.quizapp.backend.repository.QuestionRepository;
import com.quizapp.backend.repository.QuizRepository;
import com.quizapp.backend.repository.QuizSessionRepository;
import com.quizapp.backend.service.CurrentUserService;
import com.quizapp.backend.service.QuestionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final CurrentUserService currentUserService;
    private final QuizRepository quizRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Override
    @Transactional
    public QuestionDTO addQuestion(UUID quizId, QuestionCreateRequest request, Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + quizId + " not found"));

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to add questions to this quiz");
        }

        ensureQuizCanEditQuestions(quizId, quiz);
        validateQuestionCreateRequest(quizId, request);

        Question question = new Question();
        question.setText(request.text());
        question.setImageUrl(request.imageUrl());
        question.setType(request.type());
        question.setTimeLimitSeconds(request.timeLimitSeconds());
        question.setPointsCorrect(request.pointsCorrect());
        question.setPointsIncorrect(request.pointsIncorrect());
        question.setPosition(request.position());

        request.answerOptions().forEach(optionRequest -> {
            AnswerOption option = new AnswerOption();
            option.setText(optionRequest.text());
            option.setCorrect(optionRequest.correct());
            option.setPosition(optionRequest.position());

            question.addAnswer(option);
        });

        quiz.addQuestion(question);

        Question savedQuestion = questionRepository.save(question);

        return questionMapper.toQuestionDTO(savedQuestion);
    }

    @Override
    @Transactional
    public QuestionDTO updateQuestion(
        UUID quizId,
        UUID questionId,
        QuestionUpdateRequest request,
        Authentication authentication
    ) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + quizId + " not found"));

        Question question = questionRepository.findWithAnswerOptionsById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question with id: " + questionId + " not found"));

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to update questions in this quiz");
        }

        ensureQuestionBelongsToQuiz(question, quizId);
        ensureQuizCanEditQuestions(quizId, quiz);
        validateQuestionUpdateRequest(quizId, questionId, request);

        questionMapper.updateEntityFromRequest(request, question);

        question.getAnswerOptions().clear();
        request.answerOptions().forEach(optionRequest -> {
            AnswerOption option = new AnswerOption();
            option.setText(optionRequest.text());
            option.setCorrect(optionRequest.correct());
            option.setPosition(optionRequest.position());

            question.addAnswer(option);
        });

        Question savedQuestion = questionRepository.save(question);

        return questionMapper.toQuestionDTO(savedQuestion);
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID quizId, UUID questionId, Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + quizId + " not found"));

        Question question = questionRepository.findWithAnswerOptionsById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question with id: " + questionId + " not found"));

        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to delete questions from this quiz");
        }

        ensureQuestionBelongsToQuiz(question, quizId);
        ensureQuizCanEditQuestions(quizId, quiz);

        questionRepository.delete(question);
    }

    private void ensureQuestionBelongsToQuiz(Question question, UUID quizId) {
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new ResourceNotFoundException(
                "Question with id: " + question.getId() + " not found in quiz with id: " + quizId
            );
        }
    }

    private void ensureQuizCanEditQuestions(UUID quizId, Quiz quiz) {
        if (quiz.getStatus() != QuizStatus.DRAFT) {
            throw new BadRequestException("Questions can be changed only in draft quizzes");
        }

        if (quizSessionRepository.existsByQuizId(quizId)) {
            throw new BadRequestException("Questions cannot be changed after quiz has sessions");
        }
    }

    private void validateQuestionCreateRequest(UUID quizId, QuestionCreateRequest request) {
        if (questionRepository.existsByQuizIdAndPosition(quizId, request.position())) {
            throw new ConflictException("Question position already exists in this quiz");
        }

        validateAnswerOptions(request.type(), request.answerOptions());
    }

    private void validateQuestionUpdateRequest(UUID quizId, UUID questionId, QuestionUpdateRequest request) {
        questionRepository.findByQuizIdAndPosition(quizId, request.position())
            .filter(existingQuestion -> !existingQuestion.getId().equals(questionId))
            .ifPresent(existingQuestion -> {
                throw new ConflictException("Question position already exists in this quiz");
            });

        validateAnswerOptions(request.type(), request.answerOptions());
    }

    private void validateAnswerOptions(QuestionType type, List<AnswerOptionRequest> answerOptions) {
        if (answerOptions.size() < 2) {
            throw new BadRequestException("Question must have at least two answer options");
        }

        long distinctPositions = answerOptions.stream()
            .map(AnswerOptionRequest::position)
            .distinct()
            .count();

        if (distinctPositions != answerOptions.size()) {
            throw new BadRequestException("Answer option positions must be unique within question");
        }

        long correctCount = answerOptions.stream()
            .filter(option -> Boolean.TRUE.equals(option.correct()))
            .count();

        if (type == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw new BadRequestException("Single choice question must have exactly one correct answer");
        }

        if (type == QuestionType.MULTIPLE_CHOICE && correctCount < 1) {
            throw new BadRequestException("Multiple choice question must have at least one correct answer");
        }

        if (type == QuestionType.TRUE_FALSE && answerOptions.size() != 2) {
            throw new BadRequestException("True/false question must have exactly two answer options");
        }
    }
}
