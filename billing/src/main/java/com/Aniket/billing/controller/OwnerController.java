package com.Aniket.billing.controller;

import com.Aniket.billing.model.Owner;
import com.Aniket.billing.service.OwnerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/tenants/{tenantId}/owners")
public class OwnerController {
    private final OwnerService ownerService;

    @PostMapping
    public ResponseEntity<Owner> create(@PathVariable String tenantId,@RequestBody Owner owner){
        owner.setTenantId(tenantId);
        return ResponseEntity.ok(ownerService.createOwner(owner));
    }

    @GetMapping
    public ResponseEntity<List<Owner>> getAll(@PathVariable String tenantId){
        return ResponseEntity.ok(ownerService.getOwnerByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Owner> getOne(@PathVariable String tenantId,@PathVariable String id){
        return ResponseEntity.ok(ownerService.getOwnerById(tenantId,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Owner> update(@PathVariable String tenantId,@PathVariable String id, @RequestBody Owner owner){
        return ResponseEntity.ok(ownerService.update(tenantId,id,owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId,@PathVariable String id){
        ownerService.delete(tenantId,id);
        return ResponseEntity.noContent().build();
    }
}
