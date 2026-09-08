package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Payment;
import com.Aniket.billing.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    public Payment createPayment(String tenantId ,Payment payment){
        payment.setId("payment::"+ UUID.randomUUID());
        payment.setCreatedAt(Instant.now());
        payment.setTenantId(tenantId);

        Payment saved = paymentRepository.save(payment);

        invoiceService.applyPayment(tenantId,payment.getInvoiceId(),payment.getAmount());
        return saved;
    }
    public List<Payment> getPaymentByTenant(String tenantId){
        return paymentRepository.findAll().stream()
                .filter(u-> u.getTenantId().equals(tenantId))
                .toList();
    }

    public Payment getOneById(String tenantId ,String id){
        Payment payment =  paymentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payment Not Found:"+id));

        if(!payment.getTenantId().equals(tenantId)){
            throw new ResourceNotFoundException("Payment not found:"+ id);
        }
        return payment;
    }

    public Payment update(String tenantId,String id, Payment updated){
        Payment existing = getOneById(tenantId,id);
        existing.setAmount(updated.getAmount());
        existing.setMethod(updated.getMethod());
        existing.setPaymentDate(updated.getPaymentDate());
        existing.setInvoiceId(updated.getInvoiceId());
        existing.setUnitId(updated.getUnitId());

        return paymentRepository.save(existing);
    }

    public void delete(String tenantId,String id){
        getOneById(tenantId,id);
        paymentRepository.deleteById(id);
    }

}
