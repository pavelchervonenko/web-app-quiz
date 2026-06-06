package com.quizapp.backend.controller;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserProfileControllerTest {

    private static final String PARTICIPANT_EMAIL = "profile-participant@test.com";
    private static final String OTHER_USER_EMAIL = "profile-other@test.com";

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

        participant = createUser(PARTICIPANT_EMAIL, "Profile Participant", UserRole.PARTICIPANT);
        createUser(OTHER_USER_EMAIL, "Other User", UserRole.PARTICIPANT);
    }

    @Test
    public void testGetCurrentUserProfile() throws Exception {
        var response = mockMvc.perform(get("/api/users/me")
                .with(participantJwt()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var profile = om.readValue(response.getContentAsString(), CurrentUserDTO.class);

        assertThat(profile.id()).isEqualTo(participant.getId());
        assertThat(profile.email()).isEqualTo(PARTICIPANT_EMAIL);
        assertThat(profile.displayName()).isEqualTo("Profile Participant");
        assertThat(profile.role()).isEqualTo(UserRole.PARTICIPANT);
    }

    @Test
    public void testUpdateCurrentUserProfile() throws Exception {
        var request = new HashMap<String, String>();
        request.put("email", "Updated-Profile@Test.Com");
        request.put("displayName", "  Updated Participant  ");

        var response = mockMvc.perform(patch("/api/users/me")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var profile = om.readValue(response.getContentAsString(), CurrentUserDTO.class);

        assertThat(profile.id()).isEqualTo(participant.getId());
        assertThat(profile.email()).isEqualTo("updated-profile@test.com");
        assertThat(profile.displayName()).isEqualTo("Updated Participant");
        assertThat(profile.role()).isEqualTo(UserRole.PARTICIPANT);

        var updatedUser = userRepository.findById(participant.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("updated-profile@test.com");
        assertThat(updatedUser.getDisplayName()).isEqualTo("Updated Participant");
    }

    @Test
    public void testUpdateCurrentUserProfileRejectsDuplicateEmail() throws Exception {
        var request = new HashMap<String, String>();
        request.put("email", OTHER_USER_EMAIL);

        mockMvc.perform(patch("/api/users/me")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isConflict());

        var unchangedUser = userRepository.findById(participant.getId()).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo(PARTICIPANT_EMAIL);
    }

    @Test
    public void testUpdateCurrentUserProfileRejectsBlankDisplayName() throws Exception {
        var request = new HashMap<String, String>();
        request.put("displayName", "   ");

        mockMvc.perform(patch("/api/users/me")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        var unchangedUser = userRepository.findById(participant.getId()).orElseThrow();
        assertThat(unchangedUser.getDisplayName()).isEqualTo("Profile Participant");
    }

    @Test
    public void testUpdateCurrentUserProfileRejectsInvalidEmail() throws Exception {
        var request = new HashMap<String, String>();
        request.put("email", "invalid-email");

        mockMvc.perform(patch("/api/users/me")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testProfileEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    private User createUser(String email, String displayName, UserRole role) {
        var user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash("password-hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private RequestPostProcessor participantJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(participant.getEmail()).claim("role", "PARTICIPANT"))
            .authorities(new SimpleGrantedAuthority("ROLE_PARTICIPANT"));
    }
}
