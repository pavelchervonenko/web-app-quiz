package com.quizapp.backend.controller;

import com.quizapp.backend.dto.quiz.QuestionDTO;
import com.quizapp.backend.model.AnswerOption;
import com.quizapp.backend.model.Question;
import com.quizapp.backend.model.Quiz;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.QuestionType;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuestionControllerTest {

    private static final String ORGANIZER_EMAIL = "question-organizer@test.com";
    private static final String OTHER_ORGANIZER_EMAIL = "question-other-organizer@test.com";
    private static final String PARTICIPANT_EMAIL = "question-participant@test.com";

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
    private Quiz quiz;

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
        quiz = createQuiz(organizer, "Question Quiz", QuizStatus.DRAFT);
    }

    @Test
    public void testAddQuestion() throws Exception {
        var response = mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/questions")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("What is JVM?", 1))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();

        var questionDTO = om.readValue(response.getContentAsString(), QuestionDTO.class);

        assertThat(questionDTO.id()).isNotNull();
        assertThat(questionDTO.text()).isEqualTo("What is JVM?");
        assertThat(questionDTO.type()).isEqualTo(QuestionType.SINGLE_CHOICE);
        assertThat(questionDTO.position()).isEqualTo(1);
        assertThat(questionDTO.answerOptions()).hasSize(2);
        assertThat(questionRepository.findById(questionDTO.id())).isPresent();
    }

    @Test
    public void testUpdateQuestion() throws Exception {
        var question = createQuestion(quiz, "Old question", 1);

        var response = mockMvc.perform(put("/api/quizzes/" + quiz.getId() + "/questions/" + question.getId())
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("Updated question", 2))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        var questionDTO = om.readValue(response.getContentAsString(), QuestionDTO.class);

        assertThat(questionDTO.id()).isEqualTo(question.getId());
        assertThat(questionDTO.text()).isEqualTo("Updated question");
        assertThat(questionDTO.position()).isEqualTo(2);
        assertThat(questionDTO.answerOptions()).hasSize(2);

        var updatedQuestion = questionRepository.findWithAnswerOptionsById(question.getId()).orElseThrow();
        assertThat(updatedQuestion.getText()).isEqualTo("Updated question");
        assertThat(updatedQuestion.getPosition()).isEqualTo(2);
        assertThat(updatedQuestion.getAnswerOptions()).hasSize(2);
    }

    @Test
    public void testDeleteQuestion() throws Exception {
        var question = createQuestion(quiz, "Question to delete", 1);

        mockMvc.perform(delete("/api/quizzes/" + quiz.getId() + "/questions/" + question.getId())
                .with(organizerJwt()))
            .andExpect(status().isNoContent());

        assertThat(questionRepository.findById(question.getId())).isEmpty();
    }

    @Test
    public void testAddQuestionRejectsDuplicatePosition() throws Exception {
        createQuestion(quiz, "Existing question", 1);

        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/questions")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("Duplicate position", 1))))
            .andExpect(status().isConflict());
    }

    @Test
    public void testAddQuestionRejectsInvalidSingleChoiceAnswers() throws Exception {
        var request = questionRequest("Invalid single choice", 1);
        request.put("answerOptions", List.of(
            answerOption("First correct answer", true, 1),
            answerOption("Second correct answer", true, 2)
        ));

        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/questions")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testOtherOrganizerCannotAddQuestionToForeignQuiz() throws Exception {
        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/questions")
                .with(otherOrganizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("Foreign quiz", 1))))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testParticipantCannotAddQuestion() throws Exception {
        mockMvc.perform(post("/api/quizzes/" + quiz.getId() + "/questions")
                .with(participantJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("Forbidden", 1))))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testCannotChangeQuestionsInPublishedQuiz() throws Exception {
        var publishedQuiz = createQuiz(organizer, "Published Quiz", QuizStatus.PUBLISHED);

        mockMvc.perform(post("/api/quizzes/" + publishedQuiz.getId() + "/questions")
                .with(organizerJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(questionRequest("Published quiz question", 1))))
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
        var newQuiz = new Quiz();
        newQuiz.setOwner(owner);
        newQuiz.setTitle(title);
        newQuiz.setDescription("Question controller test quiz");
        newQuiz.setStatus(status);
        return quizRepository.save(newQuiz);
    }

    private Question createQuestion(Quiz questionQuiz, String text, int position) {
        var question = new Question();
        question.setQuiz(questionQuiz);
        question.setText(text);
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setTimeLimitSeconds(30);
        question.setPointsCorrect(100);
        question.setPointsIncorrect(0);
        question.setPosition(position);
        question.addAnswer(createAnswerOption("Correct answer", true, 1));
        question.addAnswer(createAnswerOption("Wrong answer", false, 2));
        return questionRepository.save(question);
    }

    private AnswerOption createAnswerOption(String text, boolean correct, int position) {
        var answerOption = new AnswerOption();
        answerOption.setText(text);
        answerOption.setCorrect(correct);
        answerOption.setPosition(position);
        return answerOption;
    }

    private HashMap<String, Object> questionRequest(String text, int position) {
        var request = new HashMap<String, Object>();
        request.put("text", text);
        request.put("imageUrl", null);
        request.put("type", QuestionType.SINGLE_CHOICE);
        request.put("timeLimitSeconds", 30);
        request.put("pointsCorrect", 100);
        request.put("pointsIncorrect", 0);
        request.put("position", position);
        request.put("answerOptions", List.of(
            answerOption("Correct answer", true, 1),
            answerOption("Wrong answer", false, 2)
        ));
        return request;
    }

    private HashMap<String, Object> answerOption(String text, boolean correct, int position) {
        var request = new HashMap<String, Object>();
        request.put("text", text);
        request.put("correct", correct);
        request.put("position", position);
        return request;
    }

    private RequestPostProcessor organizerJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(organizer.getEmail()).claim("role", "ORGANIZER"))
            .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
    }

    private RequestPostProcessor otherOrganizerJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(otherOrganizer.getEmail()).claim("role", "ORGANIZER"))
            .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
    }

    private RequestPostProcessor participantJwt() {
        return jwt()
            .jwt(jwt -> jwt.subject(participant.getEmail()).claim("role", "PARTICIPANT"))
            .authorities(new SimpleGrantedAuthority("ROLE_PARTICIPANT"));
    }
}
