package com.Aniket.billing.config;

import com.Aniket.billing.model.User;
import com.Aniket.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SUPERADMIN_USERNAME:}")
    private String username;

    @Value("${SUPERADMIN_PASSWORD:}")
    private String password;

    @Override
    public void run(ApplicationArguments args)  {
        if(username.isBlank() || password.isBlank()){
            return;
        }
        if(userRepository.findByUsername(username).isPresent()){
            return;
        }

        User admin = User.builder()
                .id("user::"+ UUID.randomUUID())
                .tenantId("platform")
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role("SUPERADMIN")
                .createdAt(Instant.now())
                .build();

        userRepository.save(admin);
        System.out.println("Seeded SUPERADMIN user: " + username);
    }
}
