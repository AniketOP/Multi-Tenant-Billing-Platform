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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Tenant {

    @Id
    private String id;
    private String name;
    private Integer billingDay;
    private Boolean active;
    private String lateFeeType;
    private Double lateFeeValue;
    private Instant createdAt;


}
