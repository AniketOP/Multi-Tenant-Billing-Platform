package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Owner;
import com.Aniket.billing.repository.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class OwnerServiceTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerService ownerService;

    private Owner owner(String id, String tenantId, String... unitIds) {
        return Owner.builder()
                .id(id)
                .tenantId(tenantId)
                .name("Name " + id)
                .email(id + "@test.com")
                .phone("9999999999")
                .unitIds(List.of(unitIds))
                .build();
    }

    // ---------- createOwner ----------

    @Test
    void createOwner_setsPrefixedIdAndCreatedAt_andSaves() {
        Owner input = owner(null, T1, "unit::a");
        when(ownerRepository.findAll()).thenReturn(List.of());
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        Owner saved = ownerService.createOwner(input);

        assertTrue(saved.getId().startsWith("owner::"));
        assertNotNull(saved.getCreatedAt());
        verify(ownerRepository).save(input);
    }

    @Test
    void createOwner_unitAlreadyAssignedInSameTenant_throwsAndDoesNotSave() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner("owner::1", T1, "unit::a")));

        assertThrows(IllegalStateException.class,
                () -> ownerService.createOwner(owner(null, T1, "unit::a")));

        verify(ownerRepository, never()).save(any());
    }

    @Test
    void createOwner_sameUnitIdInDifferentTenant_isAllowed() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner("owner::1", T2, "unit::a")));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> ownerService.createOwner(owner(null, T1, "unit::a")));
    }

    @Test
    void createOwner_nullUnitIds_skipsValidationAndSaves() {
        Owner input = Owner.builder().tenantId(T1).name("X").unitIds(null).build();
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        ownerService.createOwner(input);

        verify(ownerRepository, never()).findAll();
        verify(ownerRepository).save(input);
    }

    // ---------- getOwnerById ----------

    @Test
    void getOwnerById_found_returnsOwner() {
        Owner o = owner("owner::1", T1);
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(o));

        assertSame(o, ownerService.getOwnerById(T1, "owner::1"));
    }

    @Test
    void getOwnerById_missing_throwsNotFound() {
        when(ownerRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.getOwnerById(T1, "owner::nope"));
    }

    @Test
    void getOwnerById_wrongTenant_throwsNotFound() {
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(owner("owner::1", T2)));

        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.getOwnerById(T1, "owner::1"));
    }

    // ---------- getOwnerByTenant ----------

    @Test
    void getOwnerByTenant_returnsOnlyThatTenantsOwners() {
        Owner mine = owner("owner::1", T1);
        when(ownerRepository.findAll()).thenReturn(List.of(mine, owner("owner::2", T2)));

        List<Owner> result = ownerService.getOwnerByTenant(T1);

        assertEquals(List.of(mine), result);
    }

    // ---------- update ----------

    @Test
    void update_keepingOwnUnits_isAllowed() {
        Owner existing = owner("owner::1", T1, "unit::a");
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(existing));
        when(ownerRepository.findAll()).thenReturn(List.of(existing));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        Owner updated = owner(null, T1, "unit::a");
        updated.setName("New Name");

        Owner result = ownerService.update(T1, "owner::1", updated);

        assertEquals("New Name", result.getName());
    }

    @Test
    void update_unitOwnedByAnotherOwner_throwsAndDoesNotSave() {
        Owner existing = owner("owner::1", T1, "unit::a");
        Owner other = owner("owner::2", T1, "unit::b");
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(existing));
        when(ownerRepository.findAll()).thenReturn(List.of(existing, other));

        assertThrows(IllegalStateException.class,
                () -> ownerService.update(T1, "owner::1", owner(null, T1, "unit::b")));

        verify(ownerRepository, never()).save(any());
    }

    @Test
    void update_overwritesAllEditableFields_andKeepsIdAndTenant() {
        Owner existing = owner("owner::1", T1, "unit::a");
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(existing));
        when(ownerRepository.findAll()).thenReturn(List.of(existing));
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> inv.getArgument(0));

        Owner updated = Owner.builder()
                .name("N").email("n@test.com").phone("1").unitIds(List.of("unit::z")).build();

        ownerService.update(T1, "owner::1", updated);

        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository).save(captor.capture());
        Owner saved = captor.getValue();
        assertEquals("owner::1", saved.getId());
        assertEquals(T1, saved.getTenantId());
        assertEquals("N", saved.getName());
        assertEquals("n@test.com", saved.getEmail());
        assertEquals("1", saved.getPhone());
        assertEquals(List.of("unit::z"), saved.getUnitIds());
    }

    @Test
    void update_wrongTenant_throwsNotFound() {
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(owner("owner::1", T2)));

        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.update(T1, "owner::1", owner(null, T1)));

        verify(ownerRepository, never()).save(any());
    }

    // ---------- findOwnerForUnit ----------

    @Test
    void findOwnerForUnit_found() {
        Owner o = owner("owner::1", T1, "unit::a");
        when(ownerRepository.findAll()).thenReturn(List.of(o));

        assertSame(o, ownerService.findOwnerForUnit(T1, "unit::a"));
    }

    @Test
    void findOwnerForUnit_unitBelongsToOtherTenant_throwsNotFound() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner("owner::1", T2, "unit::a")));

        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.findOwnerForUnit(T1, "unit::a"));
    }

    // ---------- delete ----------

    @Test
    void delete_ownOwner_deletes() {
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(owner("owner::1", T1)));

        ownerService.delete(T1, "owner::1");

        verify(ownerRepository).deleteById("owner::1");
    }

    @Test
    void delete_wrongTenant_throwsAndDoesNotDelete() {
        when(ownerRepository.findById("owner::1")).thenReturn(Optional.of(owner("owner::1", T2)));

        assertThrows(ResourceNotFoundException.class,
                () -> ownerService.delete(T1, "owner::1"));

        verify(ownerRepository, never()).deleteById(anyString());
    }
}