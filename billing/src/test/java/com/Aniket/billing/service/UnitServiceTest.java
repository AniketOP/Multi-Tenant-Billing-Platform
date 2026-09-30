package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Unit;
import com.Aniket.billing.repository.UnitRepository;
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
class UnitServiceTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";

    @Mock private UnitRepository unitRepository;
    @InjectMocks private UnitService unitService;

    private Unit unit(String tenantId) {
        Unit u = mock(Unit.class);
        when(u.getTenantId()).thenReturn(tenantId);
        return u;
    }

    @Test
    void createUnit_setsPrefixedId_createdAt_andActive() {
        Unit u = mock(Unit.class);
        when(unitRepository.save(u)).thenReturn(u);

        Unit result = unitService.createUnit(u);

        assertSame(u, result);
        verify(u).setId(argThat(id -> id.startsWith("unit::")));
        verify(u).setCreatedAt(any());
        verify(u).setActive(true);
    }

    @Test
    void getUnitsByTenant_returnsOnlyThatTenantsUnits() {
        Unit mine = unit(T1);
        Unit theirs = unit(T2);
        when(unitRepository.findAll()).thenReturn(List.of(mine, theirs));

        assertEquals(List.of(mine), unitService.getUnitsByTenant(T1));
    }

    @Test
    void getActiveUnitsByTenant_excludesInactiveAndOtherTenants() {
        Unit active = unit(T1);
        when(active.getActive()).thenReturn(true);
        Unit inactive = unit(T1);
        when(inactive.getActive()).thenReturn(false);
        Unit otherTenant = unit(T2);
        when(unitRepository.findAll()).thenReturn(List.of(active, inactive, otherTenant));

        assertEquals(List.of(active), unitService.getActiveUnitsByTenant(T1));
    }

    @Test
    void getUnitById_found() {
        Unit u = unit(T1);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(u));

        assertSame(u, unitService.getUnitById(T1, "unit::1"));
    }

    @Test
    void getUnitById_missing_throwsNotFound() {
        when(unitRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> unitService.getUnitById(T1, "unit::nope"));
    }

    @Test
    void getUnitById_wrongTenant_throwsNotFound() {
        Unit other = unit(T2);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(other));

        assertThrows(ResourceNotFoundException.class,
                () -> unitService.getUnitById(T1, "unit::1"));
    }

    @Test
    void updateUnit_overwritesEditableFields() {
        Unit existing = unit(T1);
        Unit updated = mock(Unit.class);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(existing));
        when(unitRepository.save(existing)).thenReturn(existing);
        when(updated.getUnitNumber()).thenReturn("A-101");
        when(updated.getProfileId()).thenReturn("profile::p1");
        when(updated.getActive()).thenReturn(false);

        unitService.updateUnit(T1, "unit::1", updated);

        verify(existing).setUnitNumber("A-101");
        verify(existing).setProfileId("profile::p1");
        verify(existing).setActive(false);
        verify(unitRepository).save(existing);
    }

    @Test
    void updateUnit_wrongTenant_throwsNotFoundAndDoesNotSave() {
        Unit other = unit(T2);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(other));

        assertThrows(ResourceNotFoundException.class,
                () -> unitService.updateUnit(T1, "unit::1", mock(Unit.class)));

        verify(unitRepository, never()).save(any());
    }

    @Test
    void deleteUnit_ownUnit_deletes() {
        Unit own = unit(T1);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(own));

        unitService.deleteUnit(T1, "unit::1");

        verify(unitRepository).deleteById("unit::1");
    }

    @Test
    void deleteUnit_wrongTenant_throwsAndDoesNotDelete() {
        Unit other = unit(T2);
        when(unitRepository.findById("unit::1")).thenReturn(Optional.of(other));

        assertThrows(ResourceNotFoundException.class,
                () -> unitService.deleteUnit(T1, "unit::1"));

        verify(unitRepository, never()).deleteById(anyString());
    }
}