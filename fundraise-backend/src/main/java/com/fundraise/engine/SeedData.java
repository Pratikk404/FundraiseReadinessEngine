package com.fundraise.engine;

import com.fundraise.engine.entity.*;
import com.fundraise.engine.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with 5 synthetic test companies.
 * Each company is designed to trigger specific compliance failure patterns.
 *
 * Companies:
 * A - CleanTech Solutions     → No issues (baseline)
 * B - InnovateHub Technologies → Dilution math sums to 94%
 * C - GreenEnergy India        → Missing DPIIT recognition
 * D - EduLearn Platforms       → Informal ESOP promises
 * E - FinServ Solutions        → Inconsistent share classes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EquityEventRepository equityEventRepository;
    private final EsopGrantRepository esopGrantRepository;
    private final ShareClassRepository shareClassRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Seeding database with synthetic test data...");

        // Create test user
        User user = userRepository.save(User.builder()
                .name("Pratik Kalambe")
                .email("pratik@test.com")
                .password(passwordEncoder.encode("test123"))
                .role(User.Role.FOUNDER)
                .plan(User.Plan.FREE)
                .verified(true)
                .build());

        // ============================================
        // Company A: CleanTech Solutions — No issues
        // ============================================
        Company cleanTech = createCompany(user, "CleanTech Solutions Pvt Ltd",
                LocalDate.of(2023, 3, 15), Company.DpiitStatus.RECOGNIZED,
                LocalDate.of(2023, 4, 1));

        // Cap table: Founders 70%, Angel Round 20%, ESOP Pool 10% = 100% ✓
        saveEvents(cleanTech, List.of(
                event(cleanTech, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 7000000L, new BigDecimal("1.00"), LocalDate.of(2023, 4, 1)),
                event(cleanTech, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2000000L, new BigDecimal("10.00"), LocalDate.of(2024, 1, 15)),
                event(cleanTech, "ESOP Pool", EquityEvent.InstrumentType.ESOP_POOL, 1000000L, BigDecimal.ZERO, LocalDate.of(2024, 1, 15))
        ));

        saveShareClasses(cleanTech, List.of(
                shareClass(cleanTech, "Ordinary", "Standard voting rights, no preference", 7000000L),
                shareClass(cleanTech, "Preferred_A", "1x liquidation preference, anti-dilution", 2000000L),
                shareClass(cleanTech, "ESOP Pool", "Reserved for employee stock options", 1000000L)
        ));

        // ============================================
        // Company B: InnovateHub — Dilution sums to 94%
        // ============================================
        Company innovateHub = createCompany(user, "InnovateHub Technologies",
                LocalDate.of(2023, 6, 1), Company.DpiitStatus.RECOGNIZED,
                LocalDate.of(2023, 7, 1));

        // Cap table: Founders 65%, Seed 20%, ESOP 9% = 94% (missing 6%)
        saveEvents(innovateHub, List.of(
                event(innovateHub, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 6500000L, new BigDecimal("1.00"), LocalDate.of(2023, 7, 1)),
                event(innovateHub, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2000000L, new BigDecimal("15.00"), LocalDate.of(2024, 3, 1)),
                event(innovateHub, "ESOP Pool", EquityEvent.InstrumentType.ESOP_POOL, 900000L, BigDecimal.ZERO, LocalDate.of(2024, 3, 1))
        ));

        // Informal ESOP promise not in cap table
        esopGrantRepository.save(EsopGrant.builder()
                .company(innovateHub)
                .grantee("CTO Candidate")
                .shares(600000L)
                .boardApproved(false)
                .grantDate(LocalDate.of(2024, 6, 1))
                .build());

        saveShareClasses(innovateHub, List.of(
                shareClass(innovateHub, "Ordinary", "Standard voting rights", 6500000L),
                shareClass(innovateHub, "Preferred_A", "1x liquidation preference", 2000000L),
                shareClass(innovateHub, "ESOP Pool", "Employee stock options", 1500000L)
        ));

        // ============================================
        // Company C: GreenEnergy — Missing DPIIT
        // ============================================
        Company greenEnergy = createCompany(user, "GreenEnergy India Pvt Ltd",
                LocalDate.of(2024, 1, 10), Company.DpiitStatus.NOT_RECOGNIZED, null);

        saveEvents(greenEnergy, List.of(
                event(greenEnergy, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 8000000L, new BigDecimal("1.00"), LocalDate.of(2024, 2, 1)),
                event(greenEnergy, "Angel", EquityEvent.InstrumentType.PREFERRED_A, 2000000L, new BigDecimal("20.00"), LocalDate.of(2025, 1, 1)),
                event(greenEnergy, "ESOP Pool", EquityEvent.InstrumentType.ESOP_POOL, 1000000L, BigDecimal.ZERO, LocalDate.of(2025, 1, 1))
        ));

        saveShareClasses(greenEnergy, List.of(
                shareClass(greenEnergy, "Ordinary", "Standard voting rights", 8000000L),
                shareClass(greenEnergy, "Preferred_A", "1x liquidation preference, pro-rata rights", 2000000L),
                shareClass(greenEnergy, "ESOP Pool", "Employee stock options", 1000000L)
        ));

        // ============================================
        // Company D: EduLearn — Informal ESOP promises
        // ============================================
        Company eduLearn = createCompany(user, "EduLearn Platforms",
                LocalDate.of(2022, 9, 1), Company.DpiitStatus.RECOGNIZED,
                LocalDate.of(2022, 10, 1));

        saveEvents(eduLearn, List.of(
                event(eduLearn, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 7500000L, new BigDecimal("1.00"), LocalDate.of(2022, 10, 1)),
                event(eduLearn, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2000000L, new BigDecimal("12.00"), LocalDate.of(2023, 6, 1)),
                event(eduLearn, "ESOP Pool", EquityEvent.InstrumentType.ESOP_POOL, 500000L, BigDecimal.ZERO, LocalDate.of(2023, 6, 1))
        ));

        // Informal ESOP promises (verbal, not board-approved)
        esopGrantRepository.saveAll(List.of(
                EsopGrant.builder()
                        .company(eduLearn).grantee("Marketing Head").shares(200000L)
                        .boardApproved(false).grantDate(LocalDate.of(2024, 1, 1)).build(),
                EsopGrant.builder()
                        .company(eduLearn).grantee("Senior Engineer").shares(150000L)
                        .boardApproved(false).grantDate(LocalDate.of(2024, 3, 15)).build(),
                EsopGrant.builder()
                        .company(eduLearn).grantee("Product Manager").shares(100000L)
                        .boardApproved(false).grantDate(LocalDate.of(2024, 6, 1)).build()
        ));

        saveShareClasses(eduLearn, List.of(
                shareClass(eduLearn, "Ordinary", "Standard voting rights", 7500000L),
                shareClass(eduLearn, "Preferred_A", "1x liquidation preference", 2000000L),
                shareClass(eduLearn, "ESOP Pool", "Reserved for employees", 500000L)
        ));

        // ============================================
        // Company E: FinServ — Inconsistent share classes
        // ============================================
        Company finServ = createCompany(user, "FinServ Solutions",
                LocalDate.of(2023, 2, 1), Company.DpiitStatus.RECOGNIZED,
                LocalDate.of(2023, 3, 1));

        saveEvents(finServ, List.of(
                event(finServ, "Pre-Seed", EquityEvent.InstrumentType.COMMON, 6000000L, new BigDecimal("1.00"), LocalDate.of(2023, 3, 1)),
                event(finServ, "Seed", EquityEvent.InstrumentType.PREFERRED_A, 2000000L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 1)),
                event(finServ, "Seed", EquityEvent.InstrumentType.PREFERRED_B, 1500000L, new BigDecimal("25.00"), LocalDate.of(2024, 1, 1)),
                event(finServ, "ESOP Pool", EquityEvent.InstrumentType.ESOP_POOL, 500000L, BigDecimal.ZERO, LocalDate.of(2024, 1, 1))
        ));

        // Share classes from documents — Preferred_B exists in events but not declared in docs
        saveShareClasses(finServ, List.of(
                shareClass(finServ, "Ordinary", "Standard voting rights, 1 vote per share", 6000000L),
                shareClass(finServ, "Preferred_A", "1x liquidation preference, anti-dilution, pro-rata", 2000000L),
                shareClass(finServ, "ESOP Pool", "Reserved for employees", 500000L)
        ));

        log.info("Seed data created successfully!");
        log.info("Test login: pratik@test.com / test123");
        log.info("Companies: CleanTech (✓), InnovateHub (✗ dilution), GreenEnergy (✗ DPIIT), EduLearn (✗ ESOP), FinServ (✗ share classes)");
    }

    private Company createCompany(User owner, String name, LocalDate incorporation,
                                  Company.DpiitStatus dpiitStatus, LocalDate dpiitDate) {
        return companyRepository.save(Company.builder()
                .owner(owner)
                .name(name)
                .incorporationDate(incorporation)
                .dpiitStatus(dpiitStatus)
                .dpiitRecognitionDate(dpiitDate)
                .build());
    }

    private EquityEvent event(Company company, String round, EquityEvent.InstrumentType type,
                              Long shares, BigDecimal price, LocalDate date) {
        return EquityEvent.builder()
                .company(company)
                .roundName(round)
                .instrumentType(type)
                .sharesIssued(shares)
                .pricePerShare(price)
                .eventDate(date)
                .build();
    }

    private void saveEvents(Company company, List<EquityEvent> events) {
        equityEventRepository.saveAll(events);
    }

    private ShareClass shareClass(Company company, String name, String rights, Long shares) {
        return ShareClass.builder()
                .company(company)
                .className(name)
                .rightsDescription(rights)
                .totalShares(shares)
                .build();
    }

    private void saveShareClasses(Company company, List<ShareClass> classes) {
        shareClassRepository.saveAll(classes);
    }
}
