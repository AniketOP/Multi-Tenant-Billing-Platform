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
public class Invoice {
    @Id
    private String id;
    private String tenantId;
    private String unitId;
    private String ownerId;
    private String billingMonth;
    private Double openingBalance;
    private Double currentCharges;
    private Double lateFee;
    private Double adjustment;
    private Double closingBalance;
    private String status;
    private String dueDate;
    private Instant createdAt;

    private Boolean lateFeeApplied;


}
