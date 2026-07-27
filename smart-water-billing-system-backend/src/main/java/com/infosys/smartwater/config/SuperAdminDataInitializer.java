package com.infosys.smartwater.config;

import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the built-in SUPERADMIN user on application startup if no Superadmin exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        String superAdminEmail = "superadmin@smartwater.com";
        String superAdminUsername = "superadmin";

        // Ensure legacy check constraints on users role do not reject SUPERADMIN
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role");
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('SUPERADMIN', 'ADMIN', 'BUILDING_OWNER', 'RESIDENT'))");
        } catch (Exception e) {
            log.debug("Note on role check constraint update: {}", e.getMessage());
        }

        if (!userRepository.existsByEmail(superAdminEmail)) {
            log.info("No Superadmin account found. Seeding default SUPERADMIN user...");

            User superAdmin = User.builder()
                    .username(superAdminUsername)
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode("SuperAdmin@123"))
                    .phone("+919999999999")
                    .role(Role.SUPERADMIN)
                    .isEnabled(true)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();

            userRepository.save(superAdmin);
            log.info("Default SUPERADMIN account initialized successfully. Email: {}", superAdminEmail);
        } else {
            log.info("SUPERADMIN account already exists.");
        }
    }
}
