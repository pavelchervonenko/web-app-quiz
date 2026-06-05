package com.quizapp.backend.service;

import com.quizapp.backend.model.User;

import org.springframework.security.core.Authentication;

public interface CurrentUserService {

    User getCurrentUser(Authentication authentication);
}
