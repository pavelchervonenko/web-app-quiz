package com.quizapp.backend.service;

import com.quizapp.backend.dto.auth.AuthLoginRequest;
import com.quizapp.backend.dto.auth.AuthRegisterRequest;
import com.quizapp.backend.dto.auth.AuthResponse;

public interface AuthService {

    AuthResponse register(AuthRegisterRequest dto);

    AuthResponse login(AuthLoginRequest dto);
}
