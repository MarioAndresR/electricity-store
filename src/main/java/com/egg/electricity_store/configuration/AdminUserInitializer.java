package com.egg.electricity_store.configuration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.egg.electricity_store.repositories.AppUserRepository;
import com.egg.electricity_store.services.AppUserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer {
    private final AppUserRepository appUserRepository;
    private final AppUserService userService;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name}")
    private String adminName;

    @PostConstruct
    public void initAdminUser() {
        try {
            if (appUserRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Admin user [{}] already exists.", adminEmail);
                return;
            }

            userService.createAdmin(adminEmail, adminName, adminPassword);
            log.info("Default ADMIN user [{}] created successfully.", adminEmail);
        } catch (Exception e) {
            log.error("Failed to create default ADMIN user: {}", e.getMessage(), e);
        }
    }
}
