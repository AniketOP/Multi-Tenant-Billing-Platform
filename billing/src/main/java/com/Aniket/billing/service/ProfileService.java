package com.Aniket.billing.service;


import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Profile;
import com.Aniket.billing.repository.ProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Profile createProfile(Profile profile){
        profile.setId("profile::"+ UUID.randomUUID());
        profile.setCreatedAt(Instant.now());
        return profileRepository.save(profile);
    }

    public List<Profile> getProfileByTenant(String tenantId){
        return profileRepository.findAll().stream()
                .filter(u->u.getTenantId().equals(tenantId))
                .toList();
    }

    public Profile getProfileById(String tenantId,String id){
        Profile profile = profileRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Profile Not Found:"+ id));

        if(!profile.getTenantId().equals(tenantId)){
            throw new ResourceNotFoundException("Profile not found: "+ id);
        }
        return profile;
    }

    public Profile update(String tenantId,String id, Profile updated){
        Profile existing = getProfileById(tenantId,id);
        existing.setName(updated.getName());
        existing.setBaseCharge(updated.getBaseCharge());
        return profileRepository.save(existing);
    }

    public void delete(String tenantId,String id){
        getProfileById(tenantId,id);
        profileRepository.deleteById(id);
    }

}
