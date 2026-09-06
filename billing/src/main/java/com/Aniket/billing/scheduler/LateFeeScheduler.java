package com.Aniket.billing.scheduler;

import com.Aniket.billing.model.Invoice;
import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.service.InvoiceService;
import com.Aniket.billing.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LateFeeScheduler {

    private final TenantService tenantService;
    private final InvoiceService invoiceService;

    @Scheduled(cron = "0 * * * * *")
    public void applyLateFee(){
        for(Tenant tenant : tenantService.getAllActiveTenants()){
            for(Invoice invoice : invoiceService.getOverDueInvoices(tenant.getId())){
                try{
                    invoiceService.applyLateFee(tenant.getId(), invoice.getId());
                    System.out.println("Applied late fee to invoice" + invoice.getId());
                } catch (Exception e) {
                    System.err.println("Failed to apply late fee to invoice " + invoice.getId() + ": " + e.getMessage());
                }
            }
        }
    }

}
