package com.quizapp.backend.service.impl;

import com.quizapp.backend.exception.ResourceNotFoundException;
import com.quizapp.backend.model.User;
import com.quizapp.backend.repository.UserRepository;
import com.quizapp.backend.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
