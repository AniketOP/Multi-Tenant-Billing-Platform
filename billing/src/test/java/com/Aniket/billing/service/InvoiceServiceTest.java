package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Invoice;
import com.Aniket.billing.model.Owner;
import com.Aniket.billing.model.Profile;
import com.Aniket.billing.model.Tenant;
import com.Aniket.billing.model.Unit;
import com.Aniket.billing.repository.InvoiceRepository;
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
class InvoiceServiceTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";
    private static final String UNIT = "unit::a";
    private static final double DELTA = 0.001;

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private TenantService tenantService;
    @Mock private UnitService unitService;
    @Mock private ProfileService profileService;
    @Mock private OwnerService ownerService;

    @InjectMocks
    private InvoiceService invoiceService;

    // ---------- helpers ----------

    private Invoice invoice(String id, String tenantId, String unitId, String month,
                            double closing, String status, String dueDate) {
        return Invoice.builder()
                .id(id)
                .tenantId(tenantId)
                .unitId(unitId)
                .billingMonth(month)
                .openingBalance(0.0)
                .currentCharges(closing)
                .lateFee(0.0)
                .adjustment(0.0)
                .closingBalance(closing)
                .status(status)
                .dueDate(dueDate)
                .lateFeeApplied(false)
                .build();
    }

    /** Stubs unit -> profile -> owner lookups without touching baseCharge. */
    private Profile stubDeps() {
        Unit unit = mock(Unit.class);
        when(unit.getProfileId()).thenReturn("profile::p1");
        when(unitService.getUnitById(T1, UNIT)).thenReturn(unit);

        Profile profile = mock(Profile.class);
        when(profileService.getProfileById(T1, "profile::p1")).thenReturn(profile);

        when(ownerService.findOwnerForUnit(T1, UNIT))
                .thenReturn(Owner.builder().id("owner::1").tenantId(T1).build());
        return profile;
    }

    private void stubDeps(double baseCharge) {
        Profile profile = stubDeps();
        when(profile.getBaseCharge()).thenReturn(baseCharge);
    }

    private Tenant stubTenant(String type, double value) {
        Tenant tenant = mock(Tenant.class);
        when(tenant.getLateFeeType()).thenReturn(type);
        when(tenant.getLateFeeValue()).thenReturn(value);
        when(tenantService.getTenantById(T1)).thenReturn(tenant);
        return tenant;
    }

    // ---------- generatedInvoice ----------

    @Test
    void generatedInvoice_firstInvoice_hasZeroOpeningAndBaseChargeAsClosing() {
        stubDeps(3500.0);
        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.generatedInvoice(T1, UNIT, "2026-09");

        assertTrue(result.getId().startsWith("invoice::"));
        assertEquals(T1, result.getTenantId());
        assertEquals(UNIT, result.getUnitId());
        assertEquals("owner::1", result.getOwnerId());
        assertEquals("2026-09", result.getBillingMonth());
        assertEquals(0.0, result.getOpeningBalance(), DELTA);
        assertEquals(3500.0, result.getCurrentCharges(), DELTA);
        assertEquals(0.0, result.getLateFee(), DELTA);
        assertEquals(0.0, result.getAdjustment(), DELTA);
        assertEquals(3500.0, result.getClosingBalance(), DELTA);
        assertEquals("PENDING", result.getStatus());
        assertEquals("2026-09-10", result.getDueDate());
        assertFalse(result.getLateFeeApplied());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void generatedInvoice_carriesPreviousClosingBalanceAsOpening() {
        stubDeps(3500.0);
        Invoice previous = invoice("invoice::1", T1, UNIT, "2026-08", 1200.0, "PARTIAL", "2026-08-10");
        when(invoiceRepository.findAll()).thenReturn(List.of(previous));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.generatedInvoice(T1, UNIT, "2026-09");

        assertEquals(1200.0, result.getOpeningBalance(), DELTA);
        assertEquals(4700.0, result.getClosingBalance(), DELTA);
    }

    @Test
    void generatedInvoice_usesLatestMonthAsPrevious_notInsertionOrder() {
        stubDeps(1000.0);
        Invoice newer = invoice("invoice::2", T1, UNIT, "2026-08", 500.0, "PENDING", "2026-08-10");
        Invoice older = invoice("invoice::1", T1, UNIT, "2026-07", 9999.0, "PENDING", "2026-07-10");
        when(invoiceRepository.findAll()).thenReturn(List.of(newer, older));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.generatedInvoice(T1, UNIT, "2026-09");

        assertEquals(500.0, result.getOpeningBalance(), DELTA);
    }

    @Test
    void generatedInvoice_duplicateMonth_throwsAndDoesNotSave() {
        stubDeps();
        Invoice existing = invoice("invoice::1", T1, UNIT, "2026-09", 3500.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findAll()).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class,
                () -> invoiceService.generatedInvoice(T1, UNIT, "2026-09"));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void generatedInvoice_sameMonthOtherTenantOrUnit_isNotADuplicate() {
        stubDeps(3500.0);
        Invoice otherTenant = invoice("invoice::1", T2, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10");
        Invoice otherUnit = invoice("invoice::2", T1, "unit::b", "2026-09", 1.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findAll()).thenReturn(List.of(otherTenant, otherUnit));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.generatedInvoice(T1, UNIT, "2026-09");

        assertEquals(0.0, result.getOpeningBalance(), DELTA);
    }

    // ---------- applyLateFee ----------

    @Test
    void applyLateFee_percentage_addsPercentOfClosingBalance() {
        stubTenant("PERCENTAGE", 1.5);
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-08", 1000.0, "PENDING", "2026-08-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        invoiceService.applyLateFee(T1, "invoice::1");

        assertEquals(15.0, inv.getLateFee(), DELTA);
        assertEquals(1015.0, inv.getClosingBalance(), DELTA);
        assertTrue(inv.getLateFeeApplied());
        verify(invoiceRepository).save(inv);
    }

    @Test
    void applyLateFee_fixed_addsFlatAmount() {
        stubTenant("FIXED", 100.0);
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-08", 1000.0, "PENDING", "2026-08-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        invoiceService.applyLateFee(T1, "invoice::1");

        assertEquals(100.0, inv.getLateFee(), DELTA);
        assertEquals(1100.0, inv.getClosingBalance(), DELTA);
        assertTrue(inv.getLateFeeApplied());
    }

    @Test
    void applyLateFee_wrongTenantInvoice_throwsNotFoundAndDoesNotSave() {
        Invoice inv = invoice("invoice::1", T2, UNIT, "2026-08", 1000.0, "PENDING", "2026-08-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.applyLateFee(T1, "invoice::1"));

        verify(invoiceRepository, never()).save(any());
    }

    // ---------- applyPayment ----------

    @Test
    void applyPayment_partial_reducesBalanceAndMarksPartial() {
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-09", 1000.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice result = invoiceService.applyPayment(T1, "invoice::1", 400.0);

        assertEquals(600.0, result.getClosingBalance(), DELTA);
        assertEquals("PARTIAL", result.getStatus());
    }

    @Test
    void applyPayment_exact_marksPaid() {
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-09", 1000.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice result = invoiceService.applyPayment(T1, "invoice::1", 1000.0);

        assertEquals(0.0, result.getClosingBalance(), DELTA);
        assertEquals("PAID", result.getStatus());
    }

    @Test
    void applyPayment_overpay_goesNegativeAndMarksPaid() {
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-09", 1000.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice result = invoiceService.applyPayment(T1, "invoice::1", 1200.0);

        assertEquals(-200.0, result.getClosingBalance(), DELTA);
        assertEquals("PAID", result.getStatus());
    }

    @Test
    void applyPayment_wrongTenant_throwsNotFoundAndDoesNotSave() {
        Invoice inv = invoice("invoice::1", T2, UNIT, "2026-09", 1000.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.applyPayment(T1, "invoice::1", 100.0));

        verify(invoiceRepository, never()).save(any());
    }

    // ---------- getOverDueInvoices ----------

    @Test
    void getOverDueInvoices_returnsOnlyPendingOrPartialPastDueWithoutLateFee() {
        Invoice pendingPast = invoice("i1", T1, UNIT, "2000-01", 1.0, "PENDING", "2000-01-10");
        Invoice partialPast = invoice("i2", T1, UNIT, "2000-02", 1.0, "PARTIAL", "2000-02-10");
        partialPast.setLateFeeApplied(null); // null must count as "not applied"

        Invoice paidPast = invoice("i3", T1, UNIT, "2000-03", 1.0, "PAID", "2000-03-10");
        Invoice alreadyFined = invoice("i4", T1, UNIT, "2000-04", 1.0, "PENDING", "2000-04-10");
        alreadyFined.setLateFeeApplied(true);
        Invoice future = invoice("i5", T1, UNIT, "2999-01", 1.0, "PENDING", "2999-01-10");
        Invoice otherTenant = invoice("i6", T2, UNIT, "2000-05", 1.0, "PENDING", "2000-05-10");

        when(invoiceRepository.findAll()).thenReturn(
                List.of(pendingPast, partialPast, paidPast, alreadyFined, future, otherTenant));

        List<Invoice> result = invoiceService.getOverDueInvoices(T1);

        assertEquals(List.of(pendingPast, partialPast), result);
    }

    // ---------- getLastInvoiceForUnit ----------

    @Test
    void getLastInvoiceForUnit_returnsLatestMonthForThatTenantAndUnit() {
        Invoice aug = invoice("i1", T1, UNIT, "2026-08", 1.0, "PAID", "2026-08-10");
        Invoice sep = invoice("i2", T1, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10");
        Invoice otherUnit = invoice("i3", T1, "unit::b", "2026-12", 1.0, "PENDING", "2026-12-10");
        Invoice otherTenant = invoice("i4", T2, UNIT, "2026-12", 1.0, "PENDING", "2026-12-10");
        Invoice noMonth = invoice("i5", T1, UNIT, null, 1.0, "PENDING", "2026-12-10");
        when(invoiceRepository.findAll()).thenReturn(List.of(aug, sep, otherUnit, otherTenant, noMonth));

        assertSame(sep, invoiceService.getLastInvoiceForUnit(T1, UNIT));
    }

    @Test
    void getLastInvoiceForUnit_none_returnsNull() {
        when(invoiceRepository.findAll()).thenReturn(List.of());

        assertNull(invoiceService.getLastInvoiceForUnit(T1, UNIT));
    }

    // ---------- invoiceExistsForMonth ----------

    @Test
    void invoiceExistsForMonth_trueOnlyForSameTenantUnitAndMonth() {
        when(invoiceRepository.findAll()).thenReturn(
                List.of(invoice("i1", T1, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10")));

        assertTrue(invoiceService.invoiceExistsForMonth(T1, UNIT, "2026-09"));
        assertFalse(invoiceService.invoiceExistsForMonth(T1, UNIT, "2026-10"));
        assertFalse(invoiceService.invoiceExistsForMonth(T1, "unit::b", "2026-09"));
        assertFalse(invoiceService.invoiceExistsForMonth(T2, UNIT, "2026-09"));
    }

    // ---------- getInvoiceById / delete ----------

    @Test
    void getInvoiceById_found_returnsInvoice() {
        Invoice inv = invoice("invoice::1", T1, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        assertSame(inv, invoiceService.getInvoiceById(T1, "invoice::1"));
    }

    @Test
    void getInvoiceById_missing_throwsNotFound() {
        when(invoiceRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.getInvoiceById(T1, "invoice::nope"));
    }

    @Test
    void getInvoiceById_wrongTenant_throwsNotFound() {
        Invoice inv = invoice("invoice::1", T2, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.getInvoiceById(T1, "invoice::1"));
    }

    @Test
    void delete_wrongTenant_throwsAndDoesNotDelete() {
        Invoice inv = invoice("invoice::1", T2, UNIT, "2026-09", 1.0, "PENDING", "2026-09-10");
        when(invoiceRepository.findById("invoice::1")).thenReturn(Optional.of(inv));

        assertThrows(ResourceNotFoundException.class,
                () -> invoiceService.delete(T1, "invoice::1"));

        verify(invoiceRepository, never()).deleteById(anyString());
    }
}