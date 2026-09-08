package com.Aniket.billing.service;

import com.Aniket.billing.dto.AuthResponse;
import com.Aniket.billing.dto.LoginRequest;
import com.Aniket.billing.dto.SignupRequest;
import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.User;
import com.Aniket.billing.repository.UserRepository;
import com.Aniket.billing.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AuthResponse signup(SignupRequest request){
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new RuntimeException("Username already taken");
        }

        User user = User.builder()
                .id("user::"+ UUID.randomUUID())
                .tenantId(request.getTenantId())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(),user.getTenantId(),user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), user.getTenantId());
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new ResourceNotFoundException("Invalid username or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            throw new ResourceNotFoundException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(),user.getTenantId(), user.getRole());
        return new AuthResponse(token, user.getUsername(),user.getRole(),user.getTenantId());
    }


}
