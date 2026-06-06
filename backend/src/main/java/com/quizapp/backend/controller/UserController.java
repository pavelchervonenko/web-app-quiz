package com.quizapp.backend.controller;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.dto.user.OrganizedSessionHistoryDTO;
import com.quizapp.backend.dto.user.ParticipationHistoryDTO;
import com.quizapp.backend.dto.user.UserProfileUpdateRequest;
import com.quizapp.backend.service.UserHistoryService;
import com.quizapp.backend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserHistoryService userHistoryService;

    @GetMapping("/me")
    public CurrentUserDTO me(Authentication authentication) {
        return userProfileService.getCurrentUser(authentication);
    }

    @PatchMapping("/me")
    public CurrentUserDTO updateMe(
        @Valid @RequestBody UserProfileUpdateRequest request,
        Authentication authentication
    ) {
        return userProfileService.updateCurrentUser(request, authentication);
    }

    @GetMapping("/me/organized-sessions")
    public List<OrganizedSessionHistoryDTO> getMyOrganizedSessions(Authentication authentication) {
        return userHistoryService.getMyOrganizedSessions(authentication);
    }

    @GetMapping("/me/participations")
    public List<ParticipationHistoryDTO> getMyParticipations(Authentication authentication) {
        return userHistoryService.getMyParticipations(authentication);
    }
}
