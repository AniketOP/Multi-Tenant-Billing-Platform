package com.Aniket.billing.seed;

import com.Aniket.billing.model.*;
import com.Aniket.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@org.springframework.context.annotation.Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final int BATCH = 1000;

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UnitRepository unitRepository;
    private final OwnerRepository ownerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.preset:SMALL}")
    private String presetName;

    @Value("${seed.password:seedpass}")
    private String seedPassword;

    @Override
    public void run(ApplicationArguments args) {
        SeedPreset preset = SeedPreset.valueOf(presetName.trim().toUpperCase());

        if (tenantRepository.existsById(SeedDataFactory.tenantId(1))) {
            log.warn("Seed data already present, skipping. To re-seed, run: DELETE FROM `billing_load`;");
            return;
        }

        log.info("Seeding {}: {} tenants x {} units x {} months",
                preset, preset.getTenants(), preset.getUnitsPerTenant(), preset.getMonths());
        long start = System.currentTimeMillis();

        SeedDataFactory factory = new SeedDataFactory(passwordEncoder.encode(seedPassword));
        long unitCount = 0, invoiceCount = 0, paymentCount = 0;

        for (int t = 1; t <= preset.getTenants(); t++) {
            List<Profile> profiles = factory.profiles(t);
            List<Unit> units = new ArrayList<>();
            List<Owner> owners = new ArrayList<>();
            List<Invoice> invoices = new ArrayList<>();
            List<Payment> payments = new ArrayList<>();

            for (int u = 1; u <= preset.getUnitsPerTenant(); u++) {
                Profile profile = factory.profileFor(profiles, u);
                Unit unit = factory.unit(t, u, profile);
                Owner owner = factory.owner(t, u, unit);
                SeedDataFactory.Ledger ledger =
                        factory.ledger(t, u, unit, profile, owner, preset.getMonths());

                units.add(unit);
                owners.add(owner);
                invoices.addAll(ledger.invoices());
                payments.addAll(ledger.payments());
            }

            tenantRepository.save(factory.tenant(t));
            userRepository.save(factory.admin(t));
            saveInBatches(profileRepository, profiles);
            saveInBatches(unitRepository, units);
            saveInBatches(ownerRepository, owners);
            saveInBatches(invoiceRepository, invoices);
            saveInBatches(paymentRepository, payments);

            unitCount += units.size();
            invoiceCount += invoices.size();
            paymentCount += payments.size();
            log.info("Tenant {}/{} done", t, preset.getTenants());
        }

        long seconds = (System.currentTimeMillis() - start) / 1000;
        log.info("Seeded {} tenants, {} units, {} invoices, {} payments in {}s. "
                        + "Wait ~10s for the indexes to catch up before querying. Log in as seed_admin_1.",
                preset.getTenants(), unitCount, invoiceCount, paymentCount, seconds);
    }

    private <T> void saveInBatches(CrudRepository<T, String> repository, List<T> items) {
        for (int i = 0; i < items.size(); i += BATCH) {
            repository.saveAll(items.subList(i, Math.min(i + BATCH, items.size())));
        }
    }
}