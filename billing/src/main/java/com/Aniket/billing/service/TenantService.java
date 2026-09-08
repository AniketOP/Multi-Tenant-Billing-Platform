package com.Aniket.billing.service;

import java.util.UUID;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.StreamEndEvent;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;


    public Tenant createTenant(Tenant tenant){
        tenant.setId("tenant::" + UUID.randomUUID());
        tenant.setCreatedAt(Instant.now());
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    public List<Tenant> getAllActiveTenants() {
        return tenantRepository.findAll().stream()
                .filter(Tenant::getActive)
                .toList();
    }

    public List<Tenant> getAllTenant(){
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(String id){
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: "+ id));
    }

    public Tenant updateTenant(String id, Tenant updated){
        Tenant existing = getTenantById(id);
        existing.setName(updated.getName());
        existing.setBillingDay(updated.getBillingDay());
        existing.setLateFeeType(updated.getLateFeeType());
        existing.setLateFeeValue(updated.getLateFeeValue());
        existing.setActive(updated.getActive());
        return tenantRepository.save(existing);
    }

    public void deleteTenant(String id){
        tenantRepository.deleteById(id);
    }


}
