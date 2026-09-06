package com.Aniket.billing.controller;

import com.Aniket.billing.model.Profile;
import com.Aniket.billing.service.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants/{tenantId}/profiles")
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<Profile>  createProfile(@PathVariable String tenantId, @RequestBody Profile profile){
        profile.setTenantId(tenantId);
        return ResponseEntity.ok(profileService.createProfile(profile));
    }

    @GetMapping
    public ResponseEntity<List<Profile>> getAll(@PathVariable String tenantId){
        return ResponseEntity.ok(profileService.getProfileByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getOne(@PathVariable String tenantId, @PathVariable String id){
        return ResponseEntity.ok(profileService.getProfileById(tenantId,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Profile> update(@PathVariable String tenantId,@PathVariable String id , @RequestBody Profile profile){
        return ResponseEntity.ok(profileService.update(tenantId,id, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantId, @PathVariable String id){
        profileService.delete(tenantId,id);
        return ResponseEntity.noContent().build();
    }


}
