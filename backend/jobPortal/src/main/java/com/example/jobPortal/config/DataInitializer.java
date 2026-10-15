package com.example.jobPortal.config;

import com.example.jobPortal.models.Role;
import com.example.jobPortal.models.User;
import com.example.jobPortal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@velaiportal.com")) {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail("admin@velaiportal.com");
            admin.setPassword(passwordEncoder.encode("Admin@12345"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}