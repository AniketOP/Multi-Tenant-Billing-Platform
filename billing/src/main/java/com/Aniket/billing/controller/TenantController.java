package com.Aniket.billing.controller;

import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<Tenant> create(@RequestBody Tenant tenant){
        return ResponseEntity.ok(tenantService.createTenant(tenant));
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAll(){
        return ResponseEntity.ok(tenantService.getAllTenant());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getOne(@PathVariable String id){
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Tenant> update(@PathVariable String id  , @RequestBody Tenant tenant){
        return ResponseEntity.ok(tenantService.updateTenant(id, tenant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }

}
