package com.Aniket.billing.seed;

import com.Aniket.billing.model.*;

import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeedDataFactory {

    public record Ledger(List<Invoice> invoices, List<Payment> payments) {}

    private static final String[] PROFILE_NAMES = {"Standard", "Premium", "Penthouse"};
    private static final double[] BASE_CHARGES = {2500.0, 4000.0, 6000.0};
    private static final String[] METHODS = {"UPI", "CASH"};

    private final Random random = new Random(42);   // fixed seed: identical data every run
    private final String passwordHash;

    public SeedDataFactory(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public static String tenantId(int t) {
        return "tenant::seed-" + t;
    }

    public Tenant tenant(int t) {
        boolean percentage = t % 2 == 0;
        return Tenant.builder()
                .id(tenantId(t))
                .name("Seed Society " + t)
                .billingDay(1 + (t % 28))
                .active(true)
                .lateFeeType(percentage ? "PERCENTAGE" : "FIXED")
                .lateFeeValue(percentage ? 1.5 : 100.0)
                .createdAt(Instant.now())
                .build();
    }

    public User admin(int t) {
        return User.builder()
                .id("user::seed-admin-" + t)
                .tenantId(tenantId(t))
                .username("seed_admin_" + t)
                .passwordHash(passwordHash)
                .role("ADMIN")
                .createdAt(Instant.now())
                .build();
    }

    public List<Profile> profiles(int t) {
        List<Profile> profiles = new ArrayList<>();
        for (int p = 0; p < PROFILE_NAMES.length; p++) {
            profiles.add(Profile.builder()
                    .id("profile::seed-" + t + "-" + p)
                    .tenantId(tenantId(t))
                    .name(PROFILE_NAMES[p])
                    .baseCharge(BASE_CHARGES[p])
                    .createdAt(Instant.now())
                    .build());
        }
        return profiles;
    }

    public Profile profileFor(List<Profile> profiles, int u) {
        return profiles.get(u % profiles.size());
    }

    public Unit unit(int t, int u, Profile profile) {
        return Unit.builder()
                .id("unit::seed-" + t + "-" + u)
                .tenantId(tenantId(t))
                .unitNumber(String.format("U-%04d", u))
                .profileId(profile.getId())
                .active(true)
                .createdAt(Instant.now())
                .build();
    }

    public Owner owner(int t, int u, Unit unit) {
        return Owner.builder()
                .id("owner::seed-" + t + "-" + u)
                .tenantId(tenantId(t))
                .name("Owner " + t + "-" + u)
                .email("owner" + t + "." + u + "@example.com")
                .phone(String.format("9%09d", t * 1000 + u))
                .unitIds(List.of(unit.getId()))
                .createdAt(Instant.now())
                .build();
    }

    public Ledger ledger(int t, int u, Unit unit, Profile profile, Owner owner, int months) {
        List<Invoice> invoices = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();
        YearMonth now = YearMonth.now();
        double carried = 0.0;

        for (int m = months; m >= 1; m--) {
            String billingMonth = now.minusMonths(m).toString();
            String invoiceId = "invoice::seed-" + t + "-" + u + "-" + billingMonth;

            double opening = carried;
            double charges = profile.getBaseCharge();
            double closing = opening + charges;
            String status;

            double roll = random.nextDouble();
            if (roll < 0.70) {                       // paid in full
                payments.add(payment(t, u, unit, invoiceId, billingMonth, closing));
                closing = 0.0;
                status = "PAID";
            } else if (roll < 0.90) {                // half paid
                double paid = Math.round(closing / 2.0);
                payments.add(payment(t, u, unit, invoiceId, billingMonth, paid));
                closing -= paid;
                status = "PARTIAL";
            } else {                                 // unpaid, balance carries forward
                status = "PENDING";
            }

            invoices.add(Invoice.builder()
                    .id(invoiceId)
                    .tenantId(tenantId(t))
                    .unitId(unit.getId())
                    .ownerId(owner.getId())
                    .billingMonth(billingMonth)
                    .openingBalance(opening)
                    .currentCharges(charges)
                    .lateFee(0.0)
                    .adjustment(0.0)
                    .closingBalance(closing)
                    .status(status)
                    .dueDate(billingMonth + "-10")
                    .lateFeeApplied(false)
                    .createdAt(Instant.now())
                    .build());

            carried = closing;
        }
        return new Ledger(invoices, payments);
    }

    private Payment payment(int t, int u, Unit unit, String invoiceId, String billingMonth, double amount) {
        return Payment.builder()
                .id("payment::seed-" + t + "-" + u + "-" + billingMonth)
                .tenantId(tenantId(t))
                .invoiceId(invoiceId)
                .unitId(unit.getId())
                .amount(amount)
                .paymentDate(billingMonth + "-" + String.format("%02d", 1 + random.nextInt(9)))
                .method(METHODS[random.nextInt(METHODS.length)])
                .createdAt(Instant.now())
                .build();
    }
}