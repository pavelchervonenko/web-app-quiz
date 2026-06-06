package com.quizapp.backend.service;

import com.quizapp.backend.dto.user.OrganizedSessionHistoryDTO;
import com.quizapp.backend.dto.user.ParticipationHistoryDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserHistoryService {

    List<OrganizedSessionHistoryDTO> getMyOrganizedSessions(Authentication authentication);

    List<ParticipationHistoryDTO> getMyParticipations(Authentication authentication);
}
