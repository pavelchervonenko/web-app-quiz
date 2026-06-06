package com.quizapp.backend.controller;

import com.quizapp.backend.dto.user.OrganizedSessionHistoryDTO;
import com.quizapp.backend.dto.user.ParticipationHistoryDTO;
import com.quizapp.backend.model.ParticipantSession;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.QuizSession;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuizSessionStatus;
import com.quizapp.backend.model.enums.QuizStatus;
import com.quizapp.backend.model.enums.UserRole;
import com.quizapp.backend.repository.AnswerOptionRepository;
import com.quizapp.backend.repository.ParticipantAnswerOptionRepository;
import com.quizapp.backend.repository.ParticipantAnswerRepository;
import com.quizapp.backend.repository.ParticipantSessionRepository;
import com.quizapp.backend.repository.QuestionRepository;
import com.quizapp.backend.repository.QuizRepository;
import com.quizapp.backend.repository.QuizSessionRepository;
import com.quizapp.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserHistoryControllerTest {

    private static final String ORGANIZER_EMAIL = "organizer-history@test.com";
    private static final String OTHER_ORGANIZER_EMAIL = "other-organizer-history@test.com";
    private static final String PARTICIPANT_EMAIL = "participant-history@test.com";
    private static final String OTHER_PARTICIPANT_EMAIL = "other-participant-history@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuizSessionRepository quizSessionRepository;

    @Autowired
    private ParticipantSessionRepository participantSessionRepository;

    @Autowired
    private ParticipantAnswerRepository participantAnswerRepository;

    @Autowired
    private ParticipantAnswerOptionRepository participantAnswerOptionRepository;

    private User organizer;
    private User otherOrganizer;
    private User participant;
    private User otherParticipant;
    private Quiz quiz;
    private QuizSession session;
    private ParticipantSession participation;

    @BeforeEach
    public void setUp() {
        participantAnswerOptionRepository.deleteAll();
        participantAnswerRepository.deleteAll();
        participantSessionRepository.deleteAll();
        quizSessionRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        userRepository.deleteAll();

        organizer = createUser(ORGANIZER_EMAIL, UserRole.ORGANIZER);
        otherOrganizer = createUser(OTHER_ORGANIZER_EMAIL, UserRole.ORGANIZER);
        participant = createUser(PARTICIPANT_EMAIL, UserRole.PARTICIPANT);
        otherParticipant = createUser(OTHER_PARTICIPANT_EMAIL, UserRole.PARTICIPANT);

        quiz = createQuiz(organizer, "History Quiz");
        session = createSession(quiz, organizer, "HIST01", QuizSessionStatus.FINISHED);
        participation = createParticipation(
            session,
            participant,
            "Player One",
            150,
            Instant.parse("2026-06-05T10:01:00Z")
        );
        createParticipation(
            session,
            otherParticipant,
            "Player Two",
            70,
            Instant.parse("2026-06-05T10:02:00Z")
        );

        var otherQuiz = createQuiz(otherOrganizer, "Other Organizer Quiz");
        var otherSession = createSession(otherQuiz, otherOrganizer, "HIST02", QuizSessionStatus.FINISHED);
        createParticipation(
            otherSession,
            participant,
            "Player One",
            30,
            Instant.parse("2026-06-05T09:01:00Z")
        );
    }

    @Test
    public void testGetMyOrganizedSessions() throws Exception {
        var response = mockMvc.perform(get("/api/users/me/organized-sessions")
                .with(organizerJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var history = om.readValue(response.getContentAsString(), OrganizedSessionHistoryDTO[].class);

        assertThat(history).hasSize(1);
        assertThat(history[0].sessionId()).isEqualTo(session.getId());
        assertThat(history[0].quizId()).isEqualTo(quiz.getId());
        assertThat(history[0].quizTitle()).isEqualTo("History Quiz");
        assertThat(history[0].roomCode()).isEqualTo("HIST01");
        assertThat(history[0].status()).isEqualTo(QuizSessionStatus.FINISHED);
        assertThat(history[0].participantCount()).isEqualTo(2);
        assertThat(history[0].startedAt()).isEqualTo(session.getStartedAt());
        assertThat(history[0].finishedAt()).isEqualTo(session.getFinishedAt());
        assertThat(history[0].createdAt()).isNotNull();
    }

    @Test
    public void testGetMyParticipations() throws Exception {
        var response = mockMvc.perform(get("/api/users/me/participations")
                .with(participantJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var history = om.readValue(response.getContentAsString(), ParticipationHistoryDTO[].class);

        assertThat(history).hasSize(2);
        assertThat(history[0].participantSessionId()).isEqualTo(participation.getId());
        assertThat(history[0].sessionId()).isEqualTo(session.getId());
        assertThat(history[0].quizId()).isEqualTo(quiz.getId());
        assertThat(history[0].quizTitle()).isEqualTo("History Quiz");
        assertThat(history[0].roomCode()).isEqualTo("HIST01");
        assertThat(history[0].status()).isEqualTo(QuizSessionStatus.FINISHED);
        assertThat(history[0].displayName()).isEqualTo("Player One");
        assertThat(history[0].score()).isEqualTo(150);
        assertThat(history[0].joinedAt()).isEqualTo(participation.getJoinedAt());
        assertThat(history[0].finishedAt()).isEqualTo(participation.getFinishedAt());
    }

    @Test
    public void testHistoryEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me/organized-sessions"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me/participations"))
            .andExpect(status().isUnauthorized());
    }

    private User createUser(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setDisplayName(role.name());
        user.setPasswordHash("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Quiz createQuiz(User owner, String title) {
        var newQuiz = new Quiz();
        newQuiz.setOwner(owner);
        newQuiz.setTitle(title);
        newQuiz.setDescription("History test quiz");
        newQuiz.setStatus(QuizStatus.PUBLISHED);
        return quizRepository.save(newQuiz);
    }

    private QuizSession createSession(
        Quiz sessionQuiz,
        User sessionOrganizer,
        String roomCode,
        QuizSessionStatus status
    ) {
        var newSession = new QuizSession();
        newSession.setQuiz(sessionQuiz);
        newSession.setOrganizer(sessionOrganizer);
        newSession.setRoomCode(roomCode);
        newSession.setStatus(status);
        newSession.setStartedAt(Instant.parse("2026-06-05T10:00:00Z"));
        newSession.setFinishedAt(Instant.parse("2026-06-05T10:30:00Z"));
        return quizSessionRepository.save(newSession);
    }

    private ParticipantSession createParticipation(
        QuizSession quizSession,
        User user,
        String displayName,
        int score,
        Instant joinedAt
    ) {
        var newParticipation = new ParticipantSession();
        newParticipation.setQuizSession(quizSession);
        newParticipation.setUser(user);
        newParticipation.setDisplayName(displayName);
        newParticipation.setScore(score);
        newParticipation.setJoinedAt(joinedAt);
        newParticipation.setFinishedAt(Instant.parse("2026-06-05T10:25:00Z"));
        return participantSessionRepository.save(newParticipation);
    }

    private RequestPostProcessor organizerJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(organizer.getEmail()).claim("role", "ORGANIZER"))
            .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
    }

    private RequestPostProcessor participantJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(participant.getEmail()).claim("role", "PARTICIPANT"))
            .authorities(new SimpleGrantedAuthority("ROLE_PARTICIPANT"));
    }
}
