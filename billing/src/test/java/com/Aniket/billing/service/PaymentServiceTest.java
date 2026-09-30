package com.Aniket.billing.service;

import com.Aniket.billing.exception.ResourceNotFoundException;
import com.Aniket.billing.model.Payment;
import com.Aniket.billing.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
class PaymentServiceTest {

    private static final String T1 = "tenant::t1";
    private static final String T2 = "tenant::t2";

    @Mock private PaymentRepository paymentRepository;
    @Mock private InvoiceService invoiceService;

    @InjectMocks
    private PaymentService paymentService;

    // ---------- createPayment ----------

    @Test
    void createPayment_invalidInvoice_throwsAndDoesNotSavePayment() {
        Payment payment = mock(Payment.class);
        when(payment.getInvoiceId()).thenReturn("invoice::bad");
        when(invoiceService.getInvoiceById(T1, "invoice::bad"))
                .thenThrow(new ResourceNotFoundException("Invoice not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createPayment(T1, payment));

        verify(paymentRepository, never()).save(any());
        verify(invoiceService, never()).applyPayment(anyString(), anyString(), anyDouble());
    }

    @Test
    void createPayment_valid_stampsFields_savesThenAppliesToInvoice() {
        Payment payment = mock(Payment.class);
        when(payment.getInvoiceId()).thenReturn("invoice::1");
        when(payment.getAmount()).thenReturn(500.0);
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = paymentService.createPayment(T1, payment);

        assertSame(payment, result);
        verify(payment).setTenantId(T1);
        verify(payment).setCreatedAt(any());
        verify(payment).setId(argThat(id -> id.startsWith("payment::")));

        InOrder order = inOrder(invoiceService, paymentRepository);
        order.verify(invoiceService).getInvoiceById(T1, "invoice::1");
        order.verify(paymentRepository).save(payment);
        order.verify(invoiceService).applyPayment(T1, "invoice::1", 500.0);
    }

    // ---------- getOneById ----------

    @Test
    void getOneById_wrongTenant_throwsNotFound() {
        Payment p = mock(Payment.class);
        when(p.getTenantId()).thenReturn(T2);
        when(paymentRepository.findById("payment::1")).thenReturn(Optional.of(p));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getOneById(T1, "payment::1"));
    }

    @Test
    void getOneById_missing_throwsNotFound() {
        when(paymentRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.getOneById(T1, "payment::nope"));
    }

    // ---------- getPaymentByTenant ----------

    @Test
    void getPaymentByTenant_returnsOnlyThatTenantsPayments() {
        Payment mine = mock(Payment.class);
        Payment theirs = mock(Payment.class);
        when(mine.getTenantId()).thenReturn(T1);
        when(theirs.getTenantId()).thenReturn(T2);
        when(paymentRepository.findAll()).thenReturn(List.of(mine, theirs));

        assertEquals(List.of(mine), paymentService.getPaymentByTenant(T1));
    }

    // ---------- delete ----------

    @Test
    void delete_wrongTenant_throwsAndDoesNotDelete() {
        Payment p = mock(Payment.class);
        when(p.getTenantId()).thenReturn(T2);
        when(paymentRepository.findById("payment::1")).thenReturn(Optional.of(p));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.delete(T1, "payment::1"));

        verify(paymentRepository, never()).deleteById(anyString());
    }
}