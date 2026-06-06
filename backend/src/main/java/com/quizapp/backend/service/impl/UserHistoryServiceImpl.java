package com.quizapp.backend.service.impl;

import com.quizapp.backend.dto.user.OrganizedSessionHistoryDTO;
import com.quizapp.backend.dto.user.ParticipationHistoryDTO;
import com.quizapp.backend.model.ParticipantSession;
import com.quizapp.backend.model.QuizSession;
import com.quizapp.backend.model.User;
import com.quizapp.backend.repository.ParticipantSessionRepository;
import com.quizapp.backend.repository.QuizSessionRepository;
import com.quizapp.backend.service.CurrentUserService;
import com.quizapp.backend.service.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHistoryServiceImpl implements UserHistoryService {

    private final CurrentUserService currentUserService;
    private final QuizSessionRepository quizSessionRepository;
    private final ParticipantSessionRepository participantSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrganizedSessionHistoryDTO> getMyOrganizedSessions(Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        return quizSessionRepository.findAllByOrganizerIdOrderByCreatedAtDesc(currentUser.getId()).stream()
            .map(this::toOrganizedSessionHistoryDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationHistoryDTO> getMyParticipations(Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        return participantSessionRepository.findAllByUserIdOrderByJoinedAtDesc(currentUser.getId()).stream()
            .map(this::toParticipationHistoryDTO)
            .toList();
    }

    private OrganizedSessionHistoryDTO toOrganizedSessionHistoryDTO(QuizSession session) {
        return new OrganizedSessionHistoryDTO(
            session.getId(),
            session.getQuiz().getId(),
            session.getQuiz().getTitle(),
            session.getRoomCode(),
            session.getStatus(),
            session.getParticipantSessions().size(),
            session.getStartedAt(),
            session.getFinishedAt(),
            session.getCreatedAt()
        );
    }

    private ParticipationHistoryDTO toParticipationHistoryDTO(ParticipantSession participation) {
        QuizSession session = participation.getQuizSession();

        return new ParticipationHistoryDTO(
            participation.getId(),
            session.getId(),
            session.getQuiz().getId(),
            session.getQuiz().getTitle(),
            session.getRoomCode(),
            session.getStatus(),
            participation.getDisplayName(),
            participation.getScore(),
            participation.getJoinedAt(),
            participation.getFinishedAt()
        );
    }
}
