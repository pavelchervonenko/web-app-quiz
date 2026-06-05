package com.quizapp.backend.config;

import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.UserRole;
import com.quizapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("development")
@RequiredArgsConstructor
public class DevelopmentDataInitializer implements CommandLineRunner {

    private static final String ORGANIZER_EMAIL = "organizer@example.com";
    private static final String ORGANIZER_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(ORGANIZER_EMAIL)) {
            return;
        }

        User organizer = new User();
        organizer.setEmail(ORGANIZER_EMAIL);
        organizer.setDisplayName("Development Organizer");
        organizer.setPasswordHash(passwordEncoder.encode(ORGANIZER_PASSWORD));
        organizer.setRole(UserRole.ORGANIZER);

        userRepository.save(organizer);
    }
}
