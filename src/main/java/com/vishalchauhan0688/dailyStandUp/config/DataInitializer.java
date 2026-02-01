package com.vishalchauhan0688.dailyStandUp.config;

import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes system constants (Roles and Statuses) in the database on application startup.
 * This ensures that all system constants are stored in the database rather than hardcoded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StatusRepository statusRepository;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeStatuses();
    }

    private void initializeRoles() {
        log.info("Initializing system roles...");
        
        List<String> defaultRoles = Arrays.asList(
            "ADMIN",
            "OWNER",
            "MANAGER",
            "TEAM_ADMIN",
            "MEMBER"
        );

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = Role.builder()
                    .roleName(roleName)
                    .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.debug("Role already exists: {}", roleName);
            }
        }
        
        log.info("Role initialization completed.");
    }

    private void initializeStatuses() {
        log.info("Initializing system statuses...");
        
        List<String> defaultStatuses = Arrays.asList(
            "TO_DO",
            "IN_PROGRESS",
            "BLOCKED",
            "REVIEW",
            "DONE"
        );

        for (String statusName : defaultStatuses) {
            if (!statusRepository.existsByStatus(statusName)) {
                Status status = Status.builder()
                    .status(statusName)
                    .build();
                statusRepository.save(status);
                log.info("Created status: {}", statusName);
            } else {
                log.debug("Status already exists: {}", statusName);
            }
        }
        
        log.info("Status initialization completed.");
    }
}

