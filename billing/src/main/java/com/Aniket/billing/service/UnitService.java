package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Unit;
import com.Aniket.billing.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;

    public Unit createUnit(Unit unit){
        unit.setId("unit::"+ UUID.randomUUID());
        unit.setCreatedAt(Instant.now());
        unit.setActive(true);
        return unitRepository.save(unit);
    }

    public List<Unit> getUnitsByTenant(String tenantId){
        return unitRepository.findAll().stream()
                .filter(u -> u.getTenantId().equals(tenantId))
                .toList();
    }

    public List<Unit> getActiveUnitsByTenant(String tenantId){
        return unitRepository.findAll().stream()
                .filter(u -> u.getTenantId().equals(tenantId))
                .filter(Unit :: getActive)
                .toList();
    }

    public Unit getUnitById(String tenantId, String id){
        Unit unit =  unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not Found:" + id));
        if (!unit.getTenantId().equals(tenantId)){
            throw new ResourceNotFoundException("Unit not found: "+ id);
        }
        return unit;
    }

    public Unit updateUnit(String tenantId,String id ,Unit updated){
        Unit existing = getUnitById(tenantId, id);
        existing.setUnitNumber(updated.getUnitNumber());
        existing.setProfileId(updated.getProfileId());
        existing.setActive(updated.getActive());
        return unitRepository.save(existing);
    }

    public void deleteUnit(String tenantId,String id){
        getUnitById(tenantId, id);
        unitRepository.deleteById(id);
    }
}
