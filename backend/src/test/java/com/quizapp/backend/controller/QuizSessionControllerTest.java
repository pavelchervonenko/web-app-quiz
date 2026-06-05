package com.quizapp.backend.controller;

import com.quizapp.backend.dto.session.JoinQuizSessionResponse;
import com.quizapp.backend.dto.session.QuizSessionDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.dto.session.SubmittedAnswerDTO;
import com.quizapp.backend.model.AnswerOption;
import com.quizapp.backend.model.Question;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuestionType;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuizSessionControllerTest {

    private static final String ORGANIZER_EMAIL = "organizer@test.com";
    private static final String PARTICIPANT_EMAIL = "participant@test.com";

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
    private User participant;
    private Quiz quiz;
    private Question question;
    private AnswerOption correctOption;

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
        participant = createUser(PARTICIPANT_EMAIL, UserRole.PARTICIPANT);

        quiz = new Quiz();
        quiz.setOwner(organizer);
        quiz.setTitle("Smoke Quiz");
        quiz.setDescription("Controller integration test quiz");
        quiz.setStatus(QuizStatus.DRAFT);

        question = new Question();
        question.setText("What is JVM?");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setTimeLimitSeconds(30);
        question.setPointsCorrect(100);
        question.setPointsIncorrect(0);
        question.setPosition(1);

        correctOption = createAnswerOption("Java Virtual Machine", true, 1);
        var wrongOption = createAnswerOption("Java Visual Manager", false, 2);

        question.addAnswer(correctOption);
        question.addAnswer(wrongOption);
        quiz.addQuestion(question);

        quiz = quizRepository.save(quiz);
        question = quiz.getQuestions().getFirst();
        correctOption = question.getAnswerOptions().getFirst();
    }

    @Test
    public void testLiveSessionFlow() throws Exception {
        var startResponse = mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/sessions")
                .with(organizerJwt()))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var sessionDTO = om.readValue(startResponse.getContentAsString(), QuizSessionDTO.class);
        assertThat(sessionDTO.status()).isEqualTo(QuizSessionStatus.WAITING);
        assertThat(sessionDTO.roomCode()).isNotBlank();

        var joinData = new HashMap<String, String>();
        joinData.put("roomCode", sessionDTO.roomCode());
        joinData.put("displayName", "Player One");

        var joinResponse = mockMvc.perform(post("/api/sessions/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(joinData)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var joinDTO = om.readValue(joinResponse.getContentAsString(), JoinQuizSessionResponse.class);

        var nextResponse = mockMvc.perform(post("/api/sessions/" + sessionDTO.id() + "/questions/next")
                .with(organizerJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var activeState = om.readValue(nextResponse.getContentAsString(), SessionStateDTO.class);
        assertThat(activeState.status()).isEqualTo(QuizSessionStatus.QUESTION_ACTIVE);
        assertThat(activeState.currentQuestion().id()).isEqualTo(question.getId());

        var answerData = new HashMap<String, Object>();
        answerData.put("participantSessionId", joinDTO.participantSessionId());
        answerData.put("questionId", question.getId());
        answerData.put("selectedAnswerOptionIds", List.of(correctOption.getId()));

        var answerResponse = mockMvc.perform(post("/api/sessions/" + sessionDTO.id() + "/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(answerData)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var answerDTO = om.readValue(answerResponse.getContentAsString(), SubmittedAnswerDTO.class);
        assertThat(answerDTO.correct()).isTrue();
        assertThat(answerDTO.pointsAwarded()).isEqualTo(100);
        assertThat(answerDTO.totalScore()).isEqualTo(100);

        var stateResponse = mockMvc.perform(get("/api/sessions/" + sessionDTO.roomCode() + "/state"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var stateDTO = om.readValue(stateResponse.getContentAsString(), SessionStateDTO.class);
        assertThat(stateDTO.leaderboard().entries()).hasSize(1);
        assertThat(stateDTO.leaderboard().entries().getFirst().displayName()).isEqualTo("Player One");
        assertThat(stateDTO.leaderboard().entries().getFirst().score()).isEqualTo(100);

        var closeResponse = mockMvc.perform(post("/api/sessions/" + sessionDTO.id() + "/questions/current/close")
                .with(organizerJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var closedState = om.readValue(closeResponse.getContentAsString(), SessionStateDTO.class);
        assertThat(closedState.status()).isEqualTo(QuizSessionStatus.QUESTION_CLOSED);

        var finishResponse = mockMvc.perform(post("/api/sessions/" + sessionDTO.id() + "/finish")
                .with(organizerJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var finishedState = om.readValue(finishResponse.getContentAsString(), SessionStateDTO.class);
        assertThat(finishedState.status()).isEqualTo(QuizSessionStatus.FINISHED);

        var participantSession = participantSessionRepository.findById(joinDTO.participantSessionId()).orElseThrow();
        assertThat(participantSession.getScore()).isEqualTo(100);
        assertThat(participantAnswerRepository.findAll()).hasSize(1);
        assertThat(participantAnswerOptionRepository.findAll()).hasSize(1);
    }

    @Test
    public void testParticipantCannotStartSession() throws Exception {
        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/sessions")
                .with(participantJwt()))
            .andExpect(status().isForbidden());
    }

    private User createUser(String email, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setDisplayName(role.name());
        user.setPasswordHash("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private AnswerOption createAnswerOption(String text, boolean correct, int position) {
        var answerOption = new AnswerOption();
        answerOption.setText(text);
        answerOption.setCorrect(correct);
        answerOption.setPosition(position);
        return answerOption;
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
