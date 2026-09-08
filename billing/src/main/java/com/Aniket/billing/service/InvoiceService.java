package com.Aniket.billing.service;


import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.*;
import com.Aniket.billing.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TenantService tenantService;
    private final UnitService unitService;
    private final ProfileService profileService;
    private final OwnerService ownerService;

    public Invoice createInvoice(Invoice invoice){
        invoice.setId("invoice::"+ UUID.randomUUID());
        invoice.setCreatedAt(Instant.now());
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoiceByTenant(String tenantId){
        return invoiceRepository.findAll().stream()
                .filter(u->u.getTenantId().equals(tenantId))
                .toList();
    }

    public Invoice getInvoiceById(String tenantId,String id){
        Invoice invoice =  invoiceRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Invoice Not Found:"+ id));

        if(!invoice.getTenantId().equals(tenantId)){
            throw new ResourceNotFoundException("Invoice not found:"+id);
        }
        return invoice;
    }

    public Invoice update(String tenantId,String id, Invoice updated){
        Invoice existing = getInvoiceById(tenantId,id);
        existing.setStatus(updated.getStatus());
        existing.setAdjustment(updated.getAdjustment());
        existing.setBillingMonth(updated.getBillingMonth());
        existing.setDueDate(updated.getDueDate());
        existing.setClosingBalance(updated.getClosingBalance());
        existing.setLateFee(updated.getLateFee());
        existing.setOwnerId(updated.getOwnerId());
        existing.setCurrentCharges(updated.getCurrentCharges());
        existing.setOpeningBalance(updated.getOpeningBalance());
        return invoiceRepository.save(existing);

    }

    public Invoice getLastInvoiceForUnit(String tenantId , String unitId){
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getTenantId().equals(tenantId))
                .filter(inv -> inv.getUnitId().equals(unitId))
                .filter(inv -> inv.getBillingMonth() != null)
                .max(Comparator.comparing(Invoice::getBillingMonth))
                .orElse(null);
    }

    public List<Invoice> getOverDueInvoices(String tenantId){
        LocalDate today = LocalDate.now();
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getTenantId().equals(tenantId))
                .filter(inv-> "PENDING".equals(inv.getStatus()) || "PARTIAL".equals(inv.getStatus()))
                .filter(inv -> inv.getLateFeeApplied() == null || !inv.getLateFeeApplied())
                .filter(inv->LocalDate.parse(inv.getDueDate()).isBefore(today))
                .toList();
    }

    public void applyLateFee(String tenantId , String invoiceId){
        Invoice invoice = getInvoiceById(tenantId,invoiceId);
        Tenant tenant  = tenantService.getTenantById(tenantId);

        double fee;

        if("PERCENTAGE".equals(tenant.getLateFeeType())){
            fee = invoice.getClosingBalance() * (tenant.getLateFeeValue() / 100.0);
        }
        else{
            fee = tenant.getLateFeeValue();
        }

        invoice.setLateFee(invoice.getLateFee() + fee);
        invoice.setClosingBalance(invoice.getClosingBalance() + fee);
        invoice.setLateFeeApplied(true);
        invoiceRepository.save(invoice);
    }


    public Invoice generatedInvoice(String tenantId , String unitId , String billingMonth ){

        Unit unit = unitService.getUnitById(tenantId, unitId);
        Profile profile = profileService.getProfileById(tenantId , unit.getProfileId());
        Owner owner = ownerService.findOwnerForUnit(tenantId, unitId);

        Invoice previous = getLastInvoiceForUnit(tenantId, unitId);

        if (invoiceExistsForMonth(tenantId, unitId, billingMonth)) {
            throw new ResourceNotFoundException("Invoice already exists for unit " + unitId + " for " + billingMonth);
        }

        double openingBalance = (previous != null) ? previous.getClosingBalance() : 0.0;

        double currentCharges = profile.getBaseCharge();
        double lateFee = 0.0;
        double adjustment = 0.0;
        double closingBalance = openingBalance + currentCharges + lateFee + adjustment;

        Invoice invoice = Invoice.builder()
                .id("invoice::"+ UUID.randomUUID())
                .tenantId(tenantId)
                .unitId(unitId)
                .ownerId(owner.getId())
                .billingMonth(billingMonth)
                .openingBalance(openingBalance)
                .currentCharges(currentCharges)
                .lateFee(lateFee)
                .adjustment(adjustment)
                .closingBalance(closingBalance)
                .status("PENDING")
                .dueDate(billingMonth + "-10")
                .lateFeeApplied(false)
                .createdAt(Instant.now())
                .build();

        return invoiceRepository.save(invoice);


    }

    public boolean invoiceExistsForMonth(String tenantId, String unitId, String billingMonth) {
        return invoiceRepository.findAll().stream()
                .filter(inv -> inv.getTenantId().equals(tenantId))
                .filter(inv -> inv.getUnitId().equals(unitId))
                .anyMatch(inv -> billingMonth.equals(inv.getBillingMonth()));
    }

    public Invoice applyPayment(String tenantId , String invoiceId , double paymentAmount){

        Invoice invoice = getInvoiceById(tenantId , invoiceId);

        double newBalance = invoice.getClosingBalance() - paymentAmount;

        invoice.setClosingBalance(newBalance);

        if(newBalance <= 0){
            invoice.setStatus("PAID");
        }
        else{
            invoice.setStatus("PARTIAL");
        }

        return invoiceRepository.save(invoice);

    }

    public void delete(String tenantId,String id){
        getInvoiceById(tenantId,id);
        invoiceRepository.deleteById(id);
    }


}
