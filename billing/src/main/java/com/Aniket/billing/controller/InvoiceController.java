package com.Aniket.billing.controller;

import com.Aniket.billing.model.Invoice;
import com.Aniket.billing.service.InvoiceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/tenants/{tenantId}/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Invoice> create(@PathVariable String tenantId,@RequestBody Invoice invoice){
        invoice.setTenantId(tenantId);
        return ResponseEntity.ok(invoiceService.createInvoice(invoice));
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAll(@PathVariable String tenantId){
        return ResponseEntity.ok(invoiceService.getInvoiceByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getOne(@PathVariable String tenantId,@PathVariable String id){
        return ResponseEntity.ok(invoiceService.getInvoiceById(tenantId,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Invoice> update(@PathVariable String tenantId,@PathVariable String id, @RequestBody Invoice invoice){
        return ResponseEntity.ok(invoiceService.update(tenantId,id,invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId,@PathVariable String id){
        invoiceService.delete(tenantId,id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<Invoice> generate(@PathVariable String tenantId , @RequestParam String unitId , @RequestParam String billingMonth){
        return ResponseEntity.ok(invoiceService.generatedInvoice(tenantId, unitId, billingMonth));
    }

}
