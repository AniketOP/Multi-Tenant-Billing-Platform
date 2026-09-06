package com.Aniket.billing.controller;

import com.Aniket.billing.model.Unit;
import com.Aniket.billing.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<Unit> create(@PathVariable String tenantId ,@RequestBody Unit unit){
        unit.setTenantId(tenantId);
        return ResponseEntity.ok(unitService.createUnit(unit));
    }

    @GetMapping
    public ResponseEntity<List<Unit>> getAll(@PathVariable String tenantId){
        return ResponseEntity.ok(unitService.getUnitsByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Unit> getOne(@PathVariable String tenantId,@PathVariable String id){
        return ResponseEntity.ok(unitService.getUnitById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Unit> update(@PathVariable String tenantId, @PathVariable String id , @RequestBody Unit unit){
        return ResponseEntity.ok(unitService.updateUnit(tenantId,id,unit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId, @PathVariable String id){
        unitService.deleteUnit(tenantId,id);
        return ResponseEntity.noContent().build();
    }

}
