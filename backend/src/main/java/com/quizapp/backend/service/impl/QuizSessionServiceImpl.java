package com.quizapp.backend.service.impl;

import com.quizapp.backend.assembler.SessionStateAssembler;
import com.quizapp.backend.dto.session.JoinQuizSessionRequest;
import com.quizapp.backend.dto.session.JoinQuizSessionResponse;
import com.quizapp.backend.dto.session.QuizSessionDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.dto.session.SubmitAnswerRequest;
import com.quizapp.backend.dto.session.SubmittedAnswerDTO;
import com.quizapp.backend.exception.BadRequestException;
import com.quizapp.backend.exception.ConflictException;
import com.quizapp.backend.exception.ResourceNotFoundException;
import com.quizapp.backend.mapper.QuizSessionMapper;
import com.quizapp.backend.model.AnswerOption;
import com.quizapp.backend.model.ParticipantAnswer;
import com.quizapp.backend.model.ParticipantAnswerOption;
import com.quizapp.backend.model.ParticipantSession;
import com.quizapp.backend.model.Question;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.QuizSession;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuizSessionStatus;
import com.quizapp.backend.model.enums.QuizStatus;
import com.quizapp.backend.repository.AnswerOptionRepository;
import com.quizapp.backend.repository.ParticipantAnswerRepository;
import com.quizapp.backend.repository.ParticipantSessionRepository;
import com.quizapp.backend.repository.QuestionRepository;
import com.quizapp.backend.repository.QuizRepository;
import com.quizapp.backend.repository.QuizSessionRepository;
import com.quizapp.backend.realtime.SessionRealtimePublisher;
import com.quizapp.backend.service.CurrentUserService;
import com.quizapp.backend.service.QuizSessionService;
import com.quizapp.backend.util.RoomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl implements QuizSessionService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizRepository quizRepository;
    private final CurrentUserService currentUserService;
    private final RoomCodeGenerator roomCodeGenerator;
    private final QuizSessionMapper quizSessionMapper;
    private final SessionStateAssembler sessionStateAssembler;
    private final ParticipantSessionRepository participantSessionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final ParticipantAnswerRepository participantAnswerRepository;
    private final SessionRealtimePublisher sessionRealtimePublisher;

    @Override
    @Transactional
    public QuizSessionDTO startSession(UUID quizId, Authentication authentication) {

        User currentUser = currentUserService.getCurrentUser(authentication);

        Quiz quiz = quizRepository.findWithQuestionsById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz with id: " + quizId + " not found"));

        checkOwner(quiz, currentUser);
        checkQuizStatus(quiz);
        checkQuestionCount(quiz);
        checkQuizHasNoActiveSession(quizId);

        String code = generateUniqueRoomCode();

        QuizSession quizSession = new QuizSession();
        quizSession.setQuiz(quiz);
        quizSession.setOrganizer(currentUser);
        quizSession.setRoomCode(code);
        quizSession.setStatus(QuizSessionStatus.WAITING);
        quizSession.setStartedAt(Instant.now());

        QuizSession savedSession = quizSessionRepository.save(quizSession);

        publishSessionState(savedSession);

        return quizSessionMapper.toQuizSessionDTO(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionStateDTO getSessionState(String roomCode, Authentication authentication) {

        String normalizedRoomCode = roomCode.trim().toUpperCase(Locale.ROOT);

        QuizSession quizSession = quizSessionRepository.findWithStateByRoomCode(normalizedRoomCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Session with room code " + normalizedRoomCode + " not found"
            ));

        return sessionStateAssembler.toDTO(quizSession);
    }

    @Override
    @Transactional
    public JoinQuizSessionResponse joinSession(JoinQuizSessionRequest request, Authentication authentication) {

        String roomCode = request.roomCode().trim().toUpperCase(Locale.ROOT);
        String displayName = request.displayName().trim();

        QuizSession quizSession = quizSessionRepository.findByRoomCode(roomCode)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz session with room code " + roomCode + " not found"));

        if (quizSession.getStatus() != QuizSessionStatus.WAITING) {
            throw new BadRequestException("Quiz session is not accepting participants");
        }

        if (participantSessionRepository.existsByQuizSessionIdAndDisplayName(quizSession.getId(), displayName)) {
            throw new ConflictException("Display name is already taken in this quiz session");
        }

        ParticipantSession participantSession = new ParticipantSession();
        participantSession.setQuizSession(quizSession);
        participantSession.setDisplayName(displayName);
        participantSession.setScore(0);
        participantSession.setJoinedAt(Instant.now());

        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            User currentUser = currentUserService.getCurrentUser(authentication);
            participantSession.setUser(currentUser);
        }

        ParticipantSession savedParticipantSession = participantSessionRepository.save(participantSession);

        publishSessionState(quizSession);

        return new JoinQuizSessionResponse(
            quizSession.getId(),
            savedParticipantSession.getId(),
            quizSession.getRoomCode(),
            savedParticipantSession.getDisplayName(),
            quizSession.getStatus()
        );
    }

    @Override
    @Transactional
    public SessionStateDTO showNextQuestion(UUID sessionId, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        QuizSession quizSession = findSession(sessionId);

        checkOrganizer(quizSession, currentUser);
        ensureSessionCanShowNextQuestion(quizSession);

        Question nextQuestion = findNextQuestion(quizSession);
        Instant now = Instant.now();

        quizSession.setCurrentQuestion(nextQuestion);
        quizSession.setCurrentQuestionStartedAt(now);
        quizSession.setCurrentQuestionEndsAt(now.plusSeconds(nextQuestion.getTimeLimitSeconds()));
        quizSession.setStatus(QuizSessionStatus.QUESTION_ACTIVE);

        return publishSessionState(quizSession);
    }

    @Override
    @Transactional
    public SessionStateDTO closeCurrentQuestion(UUID sessionId, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        QuizSession quizSession = findSession(sessionId);

        checkOrganizer(quizSession, currentUser);

        if (quizSession.getStatus() != QuizSessionStatus.QUESTION_ACTIVE) {
            throw new BadRequestException("Only active question can be closed");
        }

        quizSession.setStatus(QuizSessionStatus.QUESTION_CLOSED);
        quizSession.setCurrentQuestionEndsAt(Instant.now());

        return publishSessionState(quizSession);
    }

    @Override
    @Transactional
    public SessionStateDTO finishSession(UUID sessionId, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        QuizSession quizSession = findSession(sessionId);

        checkOrganizer(quizSession, currentUser);

        if (quizSession.getStatus() == QuizSessionStatus.CANCELLED) {
            throw new BadRequestException("Cancelled quiz session cannot be finished");
        }

        if (quizSession.getStatus() != QuizSessionStatus.FINISHED) {
            Instant now = Instant.now();

            if (quizSession.getStatus() == QuizSessionStatus.QUESTION_ACTIVE) {
                quizSession.setCurrentQuestionEndsAt(now);
            }

            quizSession.setStatus(QuizSessionStatus.FINISHED);
            quizSession.setFinishedAt(now);
        }

        return publishSessionState(quizSession);
    }

    @Override
    @Transactional
    public SubmittedAnswerDTO submitAnswer(
        UUID sessionId,
        SubmitAnswerRequest request,
        Authentication authentication
    ) {
        QuizSession quizSession = findSession(sessionId);

        if (quizSession.getStatus() != QuizSessionStatus.QUESTION_ACTIVE) {
            throw new BadRequestException("Answers are accepted only while question is active");
        }

        Question currentQuestion = quizSession.getCurrentQuestion();

        if (currentQuestion == null) {
            throw new BadRequestException("Quiz session has no active question");
        }

        if (!currentQuestion.getId().equals(request.questionId())) {
            throw new BadRequestException("Answer must be submitted for current question");
        }

        Instant now = Instant.now();

        if (quizSession.getCurrentQuestionEndsAt() != null
            && now.isAfter(quizSession.getCurrentQuestionEndsAt())) {
            throw new BadRequestException("Question time is over");
        }

        ParticipantSession participantSession = participantSessionRepository.findById(request.participantSessionId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Participant session with id: " + request.participantSessionId() + " not found"
            ));

        if (!participantSession.getQuizSession().getId().equals(sessionId)) {
            throw new BadRequestException("Participant does not belong to this quiz session");
        }

        if (participantAnswerRepository.existsByParticipantSessionIdAndQuestionId(
            participantSession.getId(),
            currentQuestion.getId()
        )) {
            throw new ConflictException("Participant has already answered this question");
        }

        List<UUID> selectedOptionIds = validateAndNormalizeSelectedOptionIds(request);
        List<AnswerOption> questionOptions =
            answerOptionRepository.findAllByQuestionIdOrderByPositionAsc(currentQuestion.getId());
        List<AnswerOption> selectedOptions = resolveSelectedOptions(selectedOptionIds, questionOptions);

        validateSelectedOptionCount(currentQuestion, selectedOptions);

        boolean correct = isCorrectAnswer(selectedOptions, questionOptions);
        int pointsAwarded = correct ? currentQuestion.getPointsCorrect() : currentQuestion.getPointsIncorrect();

        ParticipantAnswer participantAnswer = new ParticipantAnswer();
        participantAnswer.setQuizSession(quizSession);
        participantAnswer.setParticipantSession(participantSession);
        participantAnswer.setQuestion(currentQuestion);
        participantAnswer.setCorrect(correct);
        participantAnswer.setPointsAwarded(pointsAwarded);
        participantAnswer.setAnsweredAt(now);

        selectedOptions.forEach(option -> {
            ParticipantAnswerOption selectedOption = new ParticipantAnswerOption();
            selectedOption.setAnswerOption(option);
            participantAnswer.addSelectedOption(selectedOption);
        });

        participantSession.setScore(participantSession.getScore() + pointsAwarded);

        ParticipantAnswer savedAnswer = participantAnswerRepository.save(participantAnswer);
        publishSessionState(quizSession);

        return new SubmittedAnswerDTO(
            savedAnswer.getId(),
            participantSession.getId(),
            currentQuestion.getId(),
            selectedOptionIds,
            savedAnswer.getCorrect(),
            savedAnswer.getPointsAwarded(),
            participantSession.getScore(),
            savedAnswer.getAnsweredAt()
        );
    }

    private SessionStateDTO publishSessionState(QuizSession quizSession) {
        SessionStateDTO state = sessionStateAssembler.toDTO(quizSession);
        sessionRealtimePublisher.publishState(quizSession.getRoomCode(), state);
        return state;
    }

    private void checkOwner(Quiz quiz, User currentUser) {
        if (!quiz.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to start quiz session");
        }
    }

    private void checkOrganizer(QuizSession quizSession, User currentUser) {
        if (!quizSession.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to manage this quiz session");
        }
    }

    private QuizSession findSession(UUID sessionId) {
        return quizSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz session with id: " + sessionId + " not found"));
    }

    private void ensureSessionCanShowNextQuestion(QuizSession quizSession) {
        if (quizSession.getStatus() == QuizSessionStatus.QUESTION_ACTIVE) {
            throw new BadRequestException("Close current question before showing next question");
        }

        if (quizSession.getStatus() == QuizSessionStatus.FINISHED
            || quizSession.getStatus() == QuizSessionStatus.CANCELLED) {
            throw new BadRequestException("Quiz session is already finished or cancelled");
        }
    }

    private Question findNextQuestion(QuizSession quizSession) {
        List<Question> questions = questionRepository.findAllByQuizIdOrderByPositionAsc(quizSession.getQuiz().getId());

        if (questions.isEmpty()) {
            throw new ConflictException("Quiz must have at least one question");
        }

        Question currentQuestion = quizSession.getCurrentQuestion();

        if (currentQuestion == null) {
            return questions.getFirst();
        }

        return questions.stream()
            .filter(question -> question.getPosition() > currentQuestion.getPosition())
            .findFirst()
            .orElseThrow(() -> new ConflictException("Quiz session has no more questions"));
    }

    private List<UUID> validateAndNormalizeSelectedOptionIds(SubmitAnswerRequest request) {
        Set<UUID> uniqueSelectedOptionIds = new LinkedHashSet<>(request.selectedAnswerOptionIds());

        if (uniqueSelectedOptionIds.size() != request.selectedAnswerOptionIds().size()) {
            throw new BadRequestException("Selected answer option ids must be unique");
        }

        return List.copyOf(uniqueSelectedOptionIds);
    }

    private List<AnswerOption> resolveSelectedOptions(
        List<UUID> selectedOptionIds,
        List<AnswerOption> questionOptions
    ) {
        Map<UUID, AnswerOption> optionsById = questionOptions.stream()
            .collect(Collectors.toMap(AnswerOption::getId, Function.identity()));

        return selectedOptionIds.stream()
            .map(optionId -> {
                AnswerOption option = optionsById.get(optionId);

                if (option == null) {
                    throw new BadRequestException("Selected answer option does not belong to current question");
                }

                return option;
            })
            .toList();
    }

    private void validateSelectedOptionCount(Question question, List<AnswerOption> selectedOptions) {
        switch (question.getType()) {
            case SINGLE_CHOICE, TRUE_FALSE -> {
                if (selectedOptions.size() != 1) {
                    throw new BadRequestException("This question requires exactly one selected answer option");
                }
            }
            case MULTIPLE_CHOICE -> {
                if (selectedOptions.isEmpty()) {
                    throw new BadRequestException("This question requires at least one selected answer option");
                }
            }
            default -> throw new BadRequestException("Unsupported question type: " + question.getType());
        }
    }

    private boolean isCorrectAnswer(List<AnswerOption> selectedOptions, List<AnswerOption> questionOptions) {
        Set<UUID> selectedOptionIds = selectedOptions.stream()
            .map(AnswerOption::getId)
            .collect(Collectors.toSet());

        Set<UUID> correctOptionIds = questionOptions.stream()
            .filter(option -> Boolean.TRUE.equals(option.getCorrect()))
            .map(AnswerOption::getId)
            .collect(Collectors.toSet());

        return selectedOptionIds.equals(correctOptionIds);
    }

    private void checkQuizStatus(Quiz quiz) {
        if (quiz.getStatus().equals(QuizStatus.ARCHIVED)) {
            throw new ConflictException("Quiz should not have a status: ARCHIVED");
        }
    }

    private void checkQuestionCount(Quiz quiz) {
        if (quiz.getQuestions().isEmpty()) {
            throw new ConflictException("Quiz must have at least one question");
        }
    }

    private void checkQuizHasNoActiveSession(UUID quizId) {
        boolean hasActiveSession = quizSessionRepository.existsByQuizIdAndStatusIn(
            quizId,
            List.of(
                QuizSessionStatus.WAITING,
                QuizSessionStatus.QUESTION_ACTIVE,
                QuizSessionStatus.QUESTION_CLOSED
            )
        );

        if (hasActiveSession) {
            throw new ConflictException("Quiz already has an active session");
        }
    }

    private String generateUniqueRoomCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String roomCode = roomCodeGenerator.generate();

            if (!quizSessionRepository.existsByRoomCode(roomCode)) {
                return roomCode;
            }
        }

        throw new ConflictException("Could not generate unique room code");
    }

}
