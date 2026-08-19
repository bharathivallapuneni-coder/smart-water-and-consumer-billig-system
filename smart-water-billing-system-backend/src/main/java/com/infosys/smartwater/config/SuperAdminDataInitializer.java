package com.infosys.smartwater.config;

import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.entity.TariffPlan;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.TariffPlanRepository;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Initializes or updates the built-in SUPERADMIN user, Building Owners, Residents,
 * default Apartment, default Household, and default Tariff Plan on application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String superAdminEmail = "superadmin@smartwater.com";
        String superAdminUsername = "super admin";
        String superAdminPassword = "admin123";

        // Ensure legacy check constraints on users role do not reject SUPERADMIN
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role");
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('SUPERADMIN', 'ADMIN', 'BUILDING_OWNER', 'RESIDENT'))");
        } catch (Exception e) {
            log.debug("Note on role check constraint update: {}", e.getMessage());
        }

        // Ensure households table has new columns for resident invitations
        try {
            jdbcTemplate.execute("ALTER TABLE households ADD COLUMN IF NOT EXISTS block_number VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE households ADD COLUMN IF NOT EXISTS invitation_status VARCHAR(20) DEFAULT 'PENDING'");
        } catch (Exception e) {
            log.debug("Note on households DDL update: {}", e.getMessage());
        }

        // Look for existing SUPERADMIN account by email, username, or role
        Optional<User> existingUser = userRepository.findByEmail(superAdminEmail);
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByUsername(superAdminUsername);
        }
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByUsername("superadmin");
        }
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByRole(Role.SUPERADMIN, Pageable.unpaged())
                    .stream().findFirst();
        }

        if (existingUser.isPresent()) {
            User superAdmin = existingUser.get();
            superAdmin.setUsername(superAdminUsername);
            superAdmin.setEmail(superAdminEmail);
            superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setIsEnabled(true);
            superAdmin.setApprovalStatus(ApprovalStatus.APPROVED);
            userRepository.save(superAdmin);
            log.info("SUPERADMIN account updated successfully. Username: '{}', Email: '{}'", superAdminUsername, superAdminEmail);
        } else {
            log.info("No Superadmin account found. Seeding default SUPERADMIN user...");
            User superAdmin = User.builder()
                    .username(superAdminUsername)
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .phone("+919999999999")
                    .role(Role.SUPERADMIN)
                    .isEnabled(true)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();

            userRepository.save(superAdmin);
            log.info("Default SUPERADMIN account initialized successfully. Username: '{}', Email: '{}'", superAdminUsername, superAdminEmail);
        }

        // Seed or update default BUILDING_OWNER accounts
        User lakshmiOwner = seedUserIfMissing("lakshmi.owner", "lakshmi.owner@example.com", "owner@123", "+919123456780", Role.BUILDING_OWNER);
        seedUserIfMissing("ramesh.owner", "ramesh.owner@example.com", "owner@123", "+919876543210", Role.BUILDING_OWNER);

        // Seed or update default RESIDENT accounts
        User priyaResident = seedUserIfMissing("priya.a101", "priya.nair@example.com", "resident@123", "+919988776655", Role.RESIDENT);
        seedUserIfMissing("arjun.a102", "arjun.rao@example.com", "resident@123", "+919911223344", Role.RESIDENT);

        // Seed default Apartment and Household if missing
        seedApartmentAndHousehold(lakshmiOwner, priyaResident);

        // Seed default active TariffPlan if missing
        if (tariffPlanRepository.findByIsActiveTrue().isEmpty()) {
            TariffPlan defaultPlan = TariffPlan.builder()
                    .planName("Standard Tariff 2026")
                    .ratePerUnit(new BigDecimal("12.5000"))
                    .fixedCharge(new BigDecimal("50.00"))
                    .minUnits(BigDecimal.ZERO)
                    .effectiveFrom(LocalDate.of(2026, 1, 1))
                    .isActive(true)
                    .build();
            TariffPlan savedPlan = tariffPlanRepository.save(defaultPlan);
            log.info("Seeded default active TariffPlan: 'Standard Tariff 2026' (ID: {})", savedPlan.getId());
        }
    }

    private User seedUserIfMissing(String username, String email, String rawPassword, String phone, Role role) {
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            opt = userRepository.findByUsername(username);
        }
        User user;
        if (opt.isPresent()) {
            user = opt.get();
        } else {
            user = new User();
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setRole(role);
        user.setIsEnabled(true);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        User saved = userRepository.save(user);
        log.info("Seeded user account: username='{}', email='{}', role='{}'", username, email, role);
        return saved;
    }

    private void seedApartmentAndHousehold(User owner, User resident) {
        if (apartmentRepository.findByApartmentNumber("APT-1002").isEmpty()) {
            Apartment apartment = Apartment.builder()
                    .apartmentNumber("APT-1002")
                    .buildingName("Green Valley Apartments")
                    .address("4-2-19, Lakshmipuram, Vijayawada, AP")
                    .totalFloors(5)
                    .totalHouseholds(1)
                    .buildingOwner(owner)
                    .build();

            Apartment savedApt = apartmentRepository.save(apartment);
            log.info("Seeded default Apartment: 'Green Valley Apartments' (APT-1002), ID: {}", savedApt.getId());

            if (householdRepository.findByHouseholdNumber("APT-1002-A101").isEmpty()) {
                Household household = Household.builder()
                        .householdNumber("APT-1002-A101")
                        .apartment(savedApt)
                        .ownerName("Priya Nair")
                        .contactPhone("+919988776655")
                        .flatArea(new BigDecimal("1200.00"))
                        .isMetered(true)
                        .user(resident)
                        .isActive(true)
                        .build();

                Household savedH = householdRepository.save(household);
                log.info("Seeded default Household: 'Flat A-101' (APT-1002-A101), ID: {}", savedH.getId());
            }
        }
    }
}
