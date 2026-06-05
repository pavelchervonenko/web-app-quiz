package com.quizapp.backend.controller;

import com.quizapp.backend.dto.auth.AuthLoginRequest;
import com.quizapp.backend.dto.auth.AuthRegisterRequest;
import com.quizapp.backend.dto.auth.AuthResponse;
import com.quizapp.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse auth(@RequestBody @Valid AuthLoginRequest dto) {
        return authService.login(dto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid AuthRegisterRequest dto) {
        return authService.register(dto);
    }
}
