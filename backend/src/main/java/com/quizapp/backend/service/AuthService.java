package com.quizapp.backend.service;

import com.quizapp.backend.dto.auth.AuthLoginRequest;
import com.quizapp.backend.dto.auth.AuthRegisterRequest;
import com.quizapp.backend.dto.auth.AuthResponse;

import com.quizapp.backend.exception.ConflictException;

import com.quizapp.backend.exception.ResourceNotFoundException;
import com.quizapp.backend.mapper.UserMapper;
import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.UserRole;
import com.quizapp.backend.repository.UserRepository;

import com.quizapp.backend.util.JWTUtils;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JWTUtils jwtUtils;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(AuthRegisterRequest dto) {
        String email = dto.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setDisplayName(dto.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(UserRole.PARTICIPANT);

        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser);

        return new AuthResponse(
            token,
            "Bearer",
            userMapper.toCurrentUserDTO(savedUser)
        );
    }

    @Transactional
    public AuthResponse login(AuthLoginRequest dto) {
        var authentication = new UsernamePasswordAuthenticationToken(
            dto.email().trim().toLowerCase(),
            dto.password());

        authenticationManager.authenticate(authentication);

        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtils.generateToken(user);

        return new AuthResponse(
            token,
            "Bearer",
            userMapper.toCurrentUserDTO(user)
        );
    }

}
