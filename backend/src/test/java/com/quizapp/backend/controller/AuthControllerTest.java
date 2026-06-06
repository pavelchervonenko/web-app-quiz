package com.quizapp.backend.controller;

import com.quizapp.backend.dto.auth.AuthResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

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
    }

    @Test
    public void testRegisterCreatesParticipantAndReturnsToken() throws Exception {
        var response = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(registerRequest("Auth-User@Test.Com", "Auth User"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var authResponse = om.readValue(response.getContentAsString(), AuthResponse.class);

        assertThat(authResponse.accessToken()).isNotBlank();
        assertThat(authResponse.tokenType()).isEqualTo("Bearer");
        assertThat(authResponse.user().email()).isEqualTo("auth-user@test.com");
        assertThat(authResponse.user().displayName()).isEqualTo("Auth User");
        assertThat(authResponse.user().role()).isEqualTo(UserRole.PARTICIPANT);
        assertThat(userRepository.existsByEmail("auth-user@test.com")).isTrue();
    }

    @Test
    public void testLoginReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(registerRequest("login-user@test.com", "Login User"))))
            .andExpect(status().isCreated());

        var response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(loginRequest("login-user@test.com", "password123"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var authResponse = om.readValue(response.getContentAsString(), AuthResponse.class);

        assertThat(authResponse.accessToken()).isNotBlank();
        assertThat(authResponse.tokenType()).isEqualTo("Bearer");
        assertThat(authResponse.user().email()).isEqualTo("login-user@test.com");
    }

    @Test
    public void testRegisterRejectsDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(registerRequest("duplicate@test.com", "Duplicate User"))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(registerRequest("DUPLICATE@Test.Com", "Duplicate User"))))
            .andExpect(status().isConflict());
    }

    @Test
    public void testLoginRejectsInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(registerRequest("wrong-password@test.com", "Wrong Password"))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(loginRequest("wrong-password@test.com", "password456"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterRejectsInvalidRequest() throws Exception {
        var request = registerRequest("invalid-email", "Invalid User");
        request.put("password", "short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    private HashMap<String, String> registerRequest(String email, String displayName) {
        var request = new HashMap<String, String>();
        request.put("email", email);
        request.put("password", "password123");
        request.put("displayName", displayName);
        return request;
    }

    private HashMap<String, String> loginRequest(String email, String password) {
        var request = new HashMap<String, String>();
        request.put("email", email);
        request.put("password", password);
        return request;
    }
}
