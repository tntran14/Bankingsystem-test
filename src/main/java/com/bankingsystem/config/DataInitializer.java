package com.bankingsystem.config;

import com.bankingsystem.model.CustomerType;
import com.bankingsystem.model.Role;
import com.bankingsystem.model.User;
import com.bankingsystem.model.enums.CustomerCategory;
import com.bankingsystem.model.enums.RoleName;
import com.bankingsystem.repository.CustomerTypeRepository;
import com.bankingsystem.repository.RoleRepository;
import com.bankingsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * Khởi tạo dữ liệu ban đầu: roles, customer types, và admin account.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CustomerTypeRepository customerTypeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Tạo Roles
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build());
            roleRepository.save(Role.builder().name(RoleName.ROLE_CUSTOMER).build());
            logger.info("Initialized roles: ROLE_ADMIN, ROLE_CUSTOMER");
        }

        // Tạo Customer Types
        if (customerTypeRepository.count() == 0) {
            customerTypeRepository.save(CustomerType.builder()
                    .category(CustomerCategory.INDIVIDUAL)
                    .description("Khách hàng cá nhân")
                    .maxTransactionLimit(new BigDecimal("500000000"))  // 500 triệu
                    .build());
            customerTypeRepository.save(CustomerType.builder()
                    .category(CustomerCategory.BUSINESS)
                    .description("Khách hàng doanh nghiệp")
                    .maxTransactionLimit(new BigDecimal("5000000000")) // 5 tỷ
                    .build());
            logger.info("Initialized customer types: INDIVIDUAL, BUSINESS");
        }

        // Tạo Admin account mặc định
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Collections.singleton(adminRole))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            logger.info("Initialized default admin account (admin/admin123)");
        }
    }
}
