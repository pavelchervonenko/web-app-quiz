package com.quizapp.backend.config;

import com.quizapp.backend.model.User;
import com.quizapp.backend.model.enums.UserRole;
import com.quizapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("development")
@RequiredArgsConstructor
public class DevelopmentDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.development.seed.organizer-email}")
    private String organizerEmail;

    @Value("${app.development.seed.organizer-password}")
    private String organizerPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(organizerEmail)) {
            return;
        }

        User organizer = new User();
        organizer.setEmail(organizerEmail);
        organizer.setDisplayName("Development Organizer");
        organizer.setPasswordHash(passwordEncoder.encode(organizerPassword));
        organizer.setRole(UserRole.ORGANIZER);

        userRepository.save(organizer);
    }
}
