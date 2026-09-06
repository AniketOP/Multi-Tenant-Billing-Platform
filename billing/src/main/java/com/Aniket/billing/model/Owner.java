package com.Aniket.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;

import java.time.Instant;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Owner {
    @Id
    private String id;
    private String tenantId;
    private String name;
    private String email;
    private String  phone;
    private List<String> unitIds;
    private Instant createdAt;
}
