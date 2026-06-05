package com.quizapp.backend.assembler;

import com.quizapp.backend.dto.session.CurrentQuestionDTO;
import com.quizapp.backend.dto.session.CurrentQuestionOptionDTO;
import com.quizapp.backend.dto.session.LeaderboardDTO;
import com.quizapp.backend.dto.session.LeaderboardEntryDTO;
import com.quizapp.backend.dto.session.SessionStateDTO;
import com.quizapp.backend.model.ParticipantSession;
import com.quizapp.backend.model.Question;
import com.quizapp.backend.model.QuizSession;
import com.quizapp.backend.repository.ParticipantSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SessionStateAssembler {

    private final ParticipantSessionRepository participantSessionRepository;

    public SessionStateDTO toDTO(QuizSession quizSession) {
        return new SessionStateDTO(
            quizSession.getId(),
            quizSession.getRoomCode(),
            quizSession.getStatus(),
            buildCurrentQuestionDTO(quizSession),
            buildLeaderboardDTO(quizSession)
        );
    }

    private CurrentQuestionDTO buildCurrentQuestionDTO(QuizSession quizSession) {
        Question question = quizSession.getCurrentQuestion();

        if (question == null) {
            return null;
        }

        List<CurrentQuestionOptionDTO> options = question.getAnswerOptions().stream()
            .map(option -> new CurrentQuestionOptionDTO(
                option.getId(),
                option.getText(),
                option.getPosition()
            ))
            .toList();

        return new CurrentQuestionDTO(
            question.getId(),
            question.getText(),
            question.getImageUrl(),
            question.getType(),
            question.getPosition(),
            question.getTimeLimitSeconds(),
            quizSession.getCurrentQuestionStartedAt(),
            quizSession.getCurrentQuestionEndsAt(),
            options
        );
    }

    private LeaderboardDTO buildLeaderboardDTO(QuizSession quizSession) {
        List<ParticipantSession> participants =
            participantSessionRepository.findAllByQuizSessionIdOrderByScoreDescJoinedAtAsc(quizSession.getId());

        List<LeaderboardEntryDTO> entries = IntStream.range(0, participants.size())
            .mapToObj(index -> {
                ParticipantSession participant = participants.get(index);

                return new LeaderboardEntryDTO(
                    index + 1,
                    participant.getId(),
                    participant.getDisplayName(),
                    participant.getScore()
                );
            })
            .toList();

        return new LeaderboardDTO(
            quizSession.getId(),
            quizSession.getStatus(),
            entries
        );
    }
}
