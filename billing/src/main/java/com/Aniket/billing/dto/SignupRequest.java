package com.Aniket.billing.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String tenantId;
    private String username;
    private String password;
    private String role;
}
