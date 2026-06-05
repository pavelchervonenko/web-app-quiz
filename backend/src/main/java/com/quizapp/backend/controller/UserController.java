package com.quizapp.backend.controller;

import com.quizapp.backend.dto.user.CurrentUserDTO;
import com.quizapp.backend.mapper.UserMapper;
import com.quizapp.backend.model.User;
import com.quizapp.backend.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public CurrentUserDTO me(Authentication authentication) {
        User user = currentUserService.getCurrentUser(authentication);
        return userMapper.toCurrentUserDTO(user);
    }
}
