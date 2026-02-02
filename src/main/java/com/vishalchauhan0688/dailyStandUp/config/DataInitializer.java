package com.vishalchauhan0688.dailyStandUp.config;

import com.vishalchauhan0688.dailyStandUp.model.GlobalRole;
import com.vishalchauhan0688.dailyStandUp.model.GlobalRole.GlobalRoleName;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.model.TeamRole;
import com.vishalchauhan0688.dailyStandUp.repository.GlobalRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes system constants (Roles and Statuses) in the database on
 * application startup.
 * This ensures that all system constants are stored in the database rather than
 * hardcoded.
 * 
 * IMPORTANT: This initializer now handles TWO types of roles:
 * 1. GLOBAL/SYSTEM roles (ADMIN, SUPER_ADMIN) - stored in global_roles table
 * 2. TEAM roles (OWNER, MANAGER, MEMBER, TEAM_ADMIN) - stored in roles table
 * with roleType='TEAM'
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TeamRoleRepository teamRoleRepository;
    private final GlobalRoleRepository globalRoleRepository;
    private final StatusRepository statusRepository;

    @Override
    public void run(String... args) {
        initializeGlobalRoles(); // Global/System roles (ADMIN, SUPER_ADMIN)
        initializeTeamRoles(); // Team-specific roles (OWNER, MANAGER, etc.)
        initializeStatuses();
    }

    /**
     * Initialize global/system roles.
     * These roles apply across all teams and are stored in the global_roles table.
     */
    private void initializeGlobalRoles() {
        log.info("Initializing global/system roles...");

        // ADMIN role - full system access
        if (!globalRoleRepository.existsByName(GlobalRoleName.ADMIN)) {
            GlobalRole adminRole = GlobalRole.builder()
                    .name(GlobalRoleName.ADMIN)
                    .description("Full system access - can manage all teams, projects, and users")
                    .build();
            globalRoleRepository.save(adminRole);
            log.info("Created global role: ADMIN");
        } else {
            log.debug("Global role ADMIN already exists");
        }

        // SUPER_ADMIN role - can manage other admins
        if (!globalRoleRepository.existsByName(GlobalRoleName.SUPER_ADMIN)) {
            GlobalRole superAdminRole = GlobalRole.builder()
                    .name(GlobalRoleName.SUPER_ADMIN)
                    .description("Super Admin - can manage other admins and has complete system access")
                    .build();
            globalRoleRepository.save(superAdminRole);
            log.info("Created global role: SUPER_ADMIN");
        } else {
            log.debug("Global role SUPER_ADMIN already exists");
        }

        log.info("Global role initialization completed.");
    }

    /**
     * Initialize team-specific roles.
     * These roles are per-team and stored in the roles table with roleType='TEAM'.
     */
    private void initializeTeamRoles() {
        log.info("Initializing team-specific roles...");

        // Team roles with proper roleType
        List<TeamRoleInitData> teamRoles = Arrays.asList(
                new TeamRoleInitData("OWNER", "Team owner - can manage team members and delete team"),
                new TeamRoleInitData("MANAGER", "Team manager - can manage projects and tickets"),
                new TeamRoleInitData("TEAM_ADMIN", "Team admin - can add/remove team members"),
                new TeamRoleInitData("MEMBER", "Regular team member - can create tickets and daily updates"));

        for (TeamRoleInitData roleData : teamRoles) {
            if (!teamRoleRepository.existsByName(roleData.roleName)) {
                TeamRole teamRole = TeamRole.builder()
                        .name(roleData.roleName)
                        .build();
                teamRoleRepository.save(teamRole);
                log.info("Created team role: {}", roleData.roleName);
            }
        }

        log.info("Team role initialization completed.");
    }

    /**
     * Helper class to store role initialization data
     */
    private static class TeamRoleInitData {
        final String roleName;
        final String description;

        TeamRoleInitData(String roleName, String description) {
            this.roleName = roleName;
            this.description = description;
        }
    }

    private void initializeStatuses() {
        log.info("Initializing system statuses...");

        List<String> defaultStatuses = Arrays.asList(
                "TO_DO",
                "IN_PROGRESS",
                "BLOCKED",
                "REVIEW",
                "DONE");

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
