package com.Aniket.billing.controller;

import com.Aniket.billing.model.Invoice;
import com.Aniket.billing.model.Payment;
import com.Aniket.billing.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/tenants/{tenantId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> create(@PathVariable String tenantId, @RequestBody Payment payment){
        payment.setTenantId(tenantId);
        return ResponseEntity.ok(paymentService.createPayment(tenantId,payment));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAll(@PathVariable String tenantId){
        return ResponseEntity.ok(paymentService.getPaymentByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getOne(@PathVariable String tenantId,@PathVariable String id){
        return ResponseEntity.ok(paymentService.getOneById(tenantId,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable String tenantId,@PathVariable String id , @RequestBody Payment payment){
        return ResponseEntity.ok(paymentService.update(tenantId,id,payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId,@PathVariable String id){
        paymentService.delete(tenantId,id);
        return ResponseEntity.noContent().build();
    }

}
