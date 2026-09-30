package com.Aniket.billing.controller;

import com.Aniket.billing.dto.AuthResponse;
import com.Aniket.billing.dto.LoginRequest;
import com.Aniket.billing.dto.SignupRequest;
import com.Aniket.billing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request,
             @RequestAttribute(value = "role", required = false) String callerRole,
             @RequestAttribute(value = "tenantId", required = false) String callerTenantId){
        return ResponseEntity.ok(authService.signup(request, callerRole, callerTenantId));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
