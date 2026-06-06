package com.quizapp.backend.controller;

import com.quizapp.backend.dto.quiz.QuizDetailsDTO;
import com.quizapp.backend.dto.quiz.QuizSummaryDTO;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuizControllerTest {

    private static final String ORGANIZER_EMAIL = "quiz-organizer@test.com";
    private static final String OTHER_ORGANIZER_EMAIL = "quiz-other-organizer@test.com";
    private static final String PARTICIPANT_EMAIL = "quiz-participant@test.com";

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
    }

    @Test
    public void testCreateQuiz() throws Exception {
        var response = mockMvc.perform(post("/api/quizzes")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createQuizRequest("Smoke Quiz", "Quiz description"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var quizDTO = om.readValue(response.getContentAsString(), QuizDetailsDTO.class);

        assertThat(quizDTO.id()).isNotNull();
        assertThat(quizDTO.ownerId()).isEqualTo(organizer.getId());
        assertThat(quizDTO.title()).isEqualTo("Smoke Quiz");
        assertThat(quizDTO.description()).isEqualTo("Quiz description");
        assertThat(quizDTO.status()).isEqualTo(QuizStatus.DRAFT);
        assertThat(quizDTO.questions()).isEmpty();
        assertThat(quizRepository.findById(quizDTO.id())).isPresent();
    }

    @Test
    public void testParticipantCannotCreateQuiz() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createQuizRequest("Forbidden Quiz", "Forbidden"))))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testGetMyQuizzesReturnsOnlyOwnedQuizzes() throws Exception {
        var ownedQuiz = createQuiz(organizer, "Owned Quiz", QuizStatus.DRAFT);
        createQuiz(otherOrganizer, "Other Quiz", QuizStatus.DRAFT);

        var response = mockMvc.perform(get("/api/quizzes/my")
                .with(organizerJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var quizzes = om.readValue(response.getContentAsString(), QuizSummaryDTO[].class);

        assertThat(quizzes).hasSize(1);
        assertThat(quizzes[0].id()).isEqualTo(ownedQuiz.getId());
        assertThat(quizzes[0].title()).isEqualTo("Owned Quiz");
        assertThat(quizzes[0].status()).isEqualTo(QuizStatus.DRAFT);
        assertThat(quizzes[0].questionCount()).isZero();
    }

    @Test
    public void testGetQuizRejectsOtherOwner() throws Exception {
        var otherQuiz = createQuiz(otherOrganizer, "Other Quiz", QuizStatus.DRAFT);

        mockMvc.perform(get("/api/quizzes/" + otherQuiz.getId())
                .with(organizerJwt()))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateQuiz() throws Exception {
        var quiz = createQuiz(organizer, "Old Quiz", QuizStatus.DRAFT);
        var request = updateQuizRequest("Updated Quiz", "Updated description", QuizStatus.PUBLISHED);

        var response = mockMvc.perform(put("/api/quizzes/" + quiz.getId())
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var quizDTO = om.readValue(response.getContentAsString(), QuizDetailsDTO.class);

        assertThat(quizDTO.id()).isEqualTo(quiz.getId());
        assertThat(quizDTO.title()).isEqualTo("Updated Quiz");
        assertThat(quizDTO.description()).isEqualTo("Updated description");
        assertThat(quizDTO.status()).isEqualTo(QuizStatus.PUBLISHED);

        var updatedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(updatedQuiz.getTitle()).isEqualTo("Updated Quiz");
        assertThat(updatedQuiz.getStatus()).isEqualTo(QuizStatus.PUBLISHED);
    }

    @Test
    public void testDeleteQuizWithoutSessionsDeletesQuiz() throws Exception {
        var quiz = createQuiz(organizer, "Delete Quiz", QuizStatus.DRAFT);

        mockMvc.perform(delete("/api/quizzes/" + quiz.getId())
                .with(organizerJwt()))
            .andExpect(status().isNoContent());

        assertThat(quizRepository.findById(quiz.getId())).isEmpty();
    }

    @Test
    public void testDeleteQuizWithSessionsArchivesQuiz() throws Exception {
        var quiz = createQuiz(organizer, "Archive Quiz", QuizStatus.PUBLISHED);
        createSession(quiz, organizer, "QUIZDEL1");

        mockMvc.perform(delete("/api/quizzes/" + quiz.getId())
                .with(organizerJwt()))
            .andExpect(status().isNoContent());

        var archivedQuiz = quizRepository.findById(quiz.getId()).orElseThrow();
        assertThat(archivedQuiz.getStatus()).isEqualTo(QuizStatus.ARCHIVED);
    }

    @Test
    public void testCreateQuizRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/quizzes")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createQuizRequest("", "Invalid"))))
            .andExpect(status().isBadRequest());
    }

    private User createUser(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setDisplayName(role.name());
        user.setPasswordHash("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Quiz createQuiz(User owner, String title, QuizStatus status) {
        var quiz = new Quiz();
        quiz.setOwner(owner);
        quiz.setTitle(title);
        quiz.setDescription("Controller test quiz");
        quiz.setStatus(status);
        return quizRepository.save(quiz);
    }

    private QuizSession createSession(Quiz quiz, User sessionOrganizer, String roomCode) {
        var session = new QuizSession();
        session.setQuiz(quiz);
        session.setOrganizer(sessionOrganizer);
        session.setRoomCode(roomCode);
        session.setStatus(QuizSessionStatus.FINISHED);
        return quizSessionRepository.save(session);
    }

    private HashMap<String, String> createQuizRequest(String title, String description) {
        var request = new HashMap<String, String>();
        request.put("title", title);
        request.put("description", description);
        return request;
    }

    private HashMap<String, Object> updateQuizRequest(String title, String description, QuizStatus status) {
        var request = new HashMap<String, Object>();
        request.put("title", title);
        request.put("description", description);
        request.put("status", status);
        return request;
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
