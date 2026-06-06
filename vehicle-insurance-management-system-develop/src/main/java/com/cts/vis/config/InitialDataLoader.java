package com.cts.vis.config;

import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@insurance.com";

        // Create default admin if not exists
        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ROLE_ADMIN)
                    .isActive(true) //
                    .build();

            userRepository.save(admin);

            System.out.println("Default ADMIN created - Email: " + adminEmail + " Password: admin123");
        }
    }
}