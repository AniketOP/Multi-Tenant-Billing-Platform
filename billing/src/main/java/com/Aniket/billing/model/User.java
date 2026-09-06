package com.Aniket.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class User {
    @Id
    private String id;

    private String tenantId;
    private String username;
    private String passwordHash;
    private String role;
    private Instant createdAt;
}
