package com.quizapp.backend.service.impl;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.dto.user.UserProfileUpdateRequest;
import com.quizapp.backend.exception.BadRequestException;
import com.quizapp.backend.exception.ConflictException;
import com.quizapp.backend.mapper.UserMapper;
import com.quizapp.backend.model.User;
import com.quizapp.backend.repository.UserRepository;
import com.quizapp.backend.service.CurrentUserService;
import com.quizapp.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public CurrentUserDTO getCurrentUser(Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);
        return userMapper.toCurrentUserDTO(currentUser);
    }

    @Override
    @Transactional
    public CurrentUserDTO updateCurrentUser(UserProfileUpdateRequest request, Authentication authentication) {
        User currentUser = currentUserService.getCurrentUser(authentication);

        updateEmail(request, currentUser);
        updateDisplayName(request, currentUser);

        User savedUser = userRepository.save(currentUser);
        return userMapper.toCurrentUserDTO(savedUser);
    }

    private void updateEmail(UserProfileUpdateRequest request, User currentUser) {
        if (request.email() == null) {
            return;
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("Email must not be blank");
        }

        if (!normalizedEmail.equals(currentUser.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with this email already exists");
        }

        currentUser.setEmail(normalizedEmail);
    }

    private void updateDisplayName(UserProfileUpdateRequest request, User currentUser) {
        if (request.displayName() == null) {
            return;
        }

        String displayName = request.displayName().trim();
        if (displayName.isBlank()) {
            throw new BadRequestException("Display name must not be blank");
        }

        currentUser.setDisplayName(displayName);
    }
}
