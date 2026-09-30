package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock private TenantRepository tenantRepository;
    @InjectMocks private TenantService tenantService;

    @Test
    void createTenant_setsPrefixedId_createdAt_andActive() {
        Tenant t = mock(Tenant.class);
        when(tenantRepository.save(t)).thenReturn(t);

        Tenant result = tenantService.createTenant(t);

        assertSame(t, result);
        verify(t).setId(argThat(id -> id.startsWith("tenant::")));
        verify(t).setCreatedAt(any());
        verify(t).setActive(true);
    }

    @Test
    void getAllActiveTenants_excludesInactive() {
        Tenant active = mock(Tenant.class);
        Tenant inactive = mock(Tenant.class);
        when(active.getActive()).thenReturn(true);
        when(inactive.getActive()).thenReturn(false);
        when(tenantRepository.findAll()).thenReturn(List.of(active, inactive));

        assertEquals(List.of(active), tenantService.getAllActiveTenants());
    }

    @Test
    void getAllTenant_returnsEverything() {
        Tenant a = mock(Tenant.class);
        Tenant b = mock(Tenant.class);
        when(tenantRepository.findAll()).thenReturn(List.of(a, b));

        assertEquals(List.of(a, b), tenantService.getAllTenant());
    }

    @Test
    void getTenantById_found() {
        Tenant t = mock(Tenant.class);
        when(tenantRepository.findById("tenant::1")).thenReturn(Optional.of(t));

        assertSame(t, tenantService.getTenantById("tenant::1"));
    }

    @Test
    void getTenantById_missing_throwsNotFound() {
        when(tenantRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tenantService.getTenantById("tenant::nope"));
    }

    @Test
    void updateTenant_overwritesAllEditableFields() {
        Tenant existing = mock(Tenant.class);
        Tenant updated = mock(Tenant.class);
        when(tenantRepository.findById("tenant::1")).thenReturn(Optional.of(existing));
        when(tenantRepository.save(existing)).thenReturn(existing);
        when(updated.getName()).thenReturn("New Name");
        when(updated.getBillingDay()).thenReturn(5);
        when(updated.getLateFeeType()).thenReturn("FIXED");
        when(updated.getLateFeeValue()).thenReturn(100.0);
        when(updated.getActive()).thenReturn(false);

        tenantService.updateTenant("tenant::1", updated);

        verify(existing).setName("New Name");
        verify(existing).setBillingDay(5);
        verify(existing).setLateFeeType("FIXED");
        verify(existing).setLateFeeValue(100.0);
        verify(existing).setActive(false);
        verify(tenantRepository).save(existing);
    }

    @Test
    void updateTenant_missing_throwsNotFoundAndDoesNotSave() {
        when(tenantRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tenantService.updateTenant("tenant::nope", mock(Tenant.class)));

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void deleteTenant_callsDeleteById() {
        tenantService.deleteTenant("tenant::1");

        verify(tenantRepository).deleteById("tenant::1");
    }
}