package com.quizapp.backend.service;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.dto.user.UserProfileUpdateRequest;
import org.springframework.security.core.Authentication;

public interface UserProfileService {

    CurrentUserDTO getCurrentUser(Authentication authentication);

    CurrentUserDTO updateCurrentUser(UserProfileUpdateRequest request, Authentication authentication);
}
