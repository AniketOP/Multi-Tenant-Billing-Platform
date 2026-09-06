package com.Aniket.billing.event;

import com.Aniket.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceFlowEventListener {

    private final InvoiceService invoiceService;


    @KafkaListener(topics = "tenant-invoice-due" , groupId = "billing-service")
    public void handleInvoiceDue(TenantInvoiceDueEvent event){
        try{
            invoiceService.generatedInvoice(event.getTenantId(),event.getUnitId(),event.getBillingMonth());
            System.out.println("Generated invoice for unit " + event.getUnitId() + "for Month: " + event.getBillingMonth() + ")");
        } catch (Exception e) {
            System.err.println("Failed to generate invoice for unit " + event.getUnitId() + ": " + e.getMessage());
        }
    }

}
