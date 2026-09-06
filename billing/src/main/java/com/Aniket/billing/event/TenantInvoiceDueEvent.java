package com.Aniket.billing.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantInvoiceDueEvent {
    private String tenantId;
    private String unitId;
    private String billingMonth;
}
