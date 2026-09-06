package com.Aniket.billing.scheduler;

import com.Aniket.billing.event.TenantInvoiceDueEvent;
import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.model.Unit;
import com.Aniket.billing.service.TenantService;
import com.Aniket.billing.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class InvoiceGenerationScheduler {

    private final TenantService tenantService;
    private final UnitService unitService;

    private final KafkaTemplate<String , TenantInvoiceDueEvent> kafkaTemplate;
    private static final String TOPIC = "tenant-invoice-due";

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Scheduled(cron = "0 0 1 * * *")
    public void generateDueInvoice(){
        LocalDate today = LocalDate.now();
        String billingMonth = today.format(MONTH_FORMAT);


        for(Tenant tenant : tenantService.getAllActiveTenants()){
            if(tenant.getBillingDay() != today.getDayOfMonth()){
                continue;
            }

            for(Unit unit : unitService.getActiveUnitsByTenant(tenant.getId())){
                TenantInvoiceDueEvent event = new TenantInvoiceDueEvent(tenant.getId(), unit.getId() , billingMonth);
                kafkaTemplate.send(TOPIC,tenant.getId() , event);
            }
        }
    }

}
