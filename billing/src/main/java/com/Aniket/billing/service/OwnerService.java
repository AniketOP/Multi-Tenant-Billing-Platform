package com.Aniket.billing.service;

import com.Aniket.billing.model.Owner;
import com.Aniket.billing.repository.OwnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public Owner createOwner(@RequestBody Owner owner){
        owner.setId("owner::"+ UUID.randomUUID());
        owner.setCreatedAt(Instant.now());
        return ownerRepository.save(owner);
    }

    public List<Owner> getOwnerByTenant(String tenantId){
        return ownerRepository.findAll().stream()
                .filter(u-> u.getTenantId().equals(tenantId))
                .toList();
    }

    public Owner getOwnerById(String tenantId ,String id){
        Owner owner =  ownerRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Owner Not Found:"+ id));

        if(!owner.getTenantId().equals(tenantId)){
            throw new RuntimeException("Owner not found: "+ id);
        }
        return owner;
    }

    public Owner update(String tenantId ,String id ,Owner updated){
        Owner existing = getOwnerById(tenantId,id);
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setUnitIds(updated.getUnitIds());
        return ownerRepository.save(existing);
    }

    public Owner findOwnerForUnit(String tenantId , String unitId){
        return ownerRepository.findAll().stream()
                .filter(o -> o.getTenantId().equals(tenantId))
                .filter(o-> o.getUnitIds() != null && o.getUnitIds().contains(unitId))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("No owner found for the unit: "+ unitId));
    }

    public void delete(String tenantId,String id){
        getOwnerById(tenantId,id);
        ownerRepository.deleteById(id);
    }


}
