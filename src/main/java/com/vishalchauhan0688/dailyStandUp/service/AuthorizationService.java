package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.*;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeGlobalRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.GlobalRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Centralized authorization service for RBAC checks.
 * 
 * IMPORTANT: This service handles TWO types of roles:
 * 1. GLOBAL/SYSTEM roles (ADMIN, SUPER_ADMIN) - stored in employee_global_roles
 * 2. TEAM roles (OWNER, MANAGER, MEMBER) - stored in employee_team_roles
 * 
 * Global roles apply everywhere, while team roles are per-team.
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;
    private final EmployeeGlobalRoleRepository employeeGlobalRoleRepository;
    private final GlobalRoleRepository globalRoleRepository;
    private final ProjectRepository projectRepository;

    // ===================== GLOBAL/SYSTEM ROLE CHECKS =====================

    /**
     * Check if user is global ADMIN (applies everywhere, regardless of team
     * membership)
     */
    public boolean isGlobalAdmin(Long employeeId) {
        return globalRoleRepository.isGlobalAdmin(employeeId);
    }

    /**
     * Check if user is SUPER_ADMIN (can manage other admins)
     */
    public boolean isSuperAdmin(Long employeeId) {
        Optional<GlobalRole> superAdminRole = globalRoleRepository.findByName(GlobalRole.GlobalRoleName.SUPER_ADMIN);
        if (superAdminRole.isEmpty()) {
            return false;
        }
        return employeeGlobalRoleRepository.existsByEmployeeIdAndGlobalRoleId(
                employeeId, superAdminRole.get().getId());
    }

    /**
     * Check if user has any global role
     */
    public boolean hasAnyGlobalRole(Long employeeId) {
        return globalRoleRepository.hasAnyGlobalRole(employeeId);
    }

    /**
     * Get all global role names for a user
     */
    public List<String> getGlobalRoleNames(Long employeeId) {
        return globalRoleRepository.findByEmployeeId(employeeId)
                .stream()
                .map(gr -> gr.getName().name())
                .toList();
    }

    // ===================== TEAM ROLE CHECKS =====================

    /**
     * Check if user has specific role in team
     * Note: This ONLY checks team roles, not global roles.
     * Use hasSystemOrTeamRole() to check both.
     */
    public boolean hasRoleInTeam(Long employeeId, Long teamId, String... roleNames) {
        Optional<EmployeeTeamRole> teamRole = employeeTeamRoleRepository.findByEmployeeIdAndTeamId(employeeId, teamId);
        if (teamRole.isEmpty()) {
            return false;
        }
        String userRole = teamRole.get().getTeamRole().getName();
        for (String roleName : roleNames) {
            if (roleName.equalsIgnoreCase(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is team member (has any role in team)
     */
    public boolean isTeamMember(Long employeeId, Long teamId) {
        return employeeTeamRoleRepository.existsByEmployeeIdAndTeamId(employeeId, teamId);
    }

    /**
     * Get user's role in team
     */
    public String getRoleInTeam(Long employeeId, Long teamId) {
        return employeeTeamRoleRepository.findByEmployeeIdAndTeamId(employeeId, teamId)
                .map(etr -> etr.getTeamRole().getName())
                .orElse(null);
    }

    // ===================== COMBINED CHECKS =====================

    /**
     * Check if user has system role OR specific team role.
     * Global admins have implicit access to all team operations.
     */
    public boolean hasSystemOrTeamRole(Long employeeId, Long teamId, String... teamRoleNames) {
        // First check if user is a global admin (has access to everything)
        if (isGlobalAdmin(employeeId)) {
            return true;
        }
        // Otherwise check team-specific roles
        return hasRoleInTeam(employeeId, teamId, teamRoleNames);
    }

    /**
     * Get all roles (both global and team) for a user
     */
    public List<String> getAllRoles(Long employeeId, Long teamId) {
        List<String> roles = new java.util.ArrayList<>();

        // Add global roles
        roles.addAll(getGlobalRoleNames(employeeId));

        // Add team role if member of this team
        String teamRole = getRoleInTeam(employeeId, teamId);
        if (teamRole != null && !roles.contains(teamRole)) {
            roles.add(teamRole);
        }

        return roles;
    }

    // ===================== PROJECT PERMISSIONS =====================

    /**
     * Verify user can create project in team
     * Allowed: Global ADMIN, or OWNER/MANAGER/TEAM_ADMIN in team
     */
    public void verifyCanCreateProject(Long employeeId, Long teamId) {
        if (isGlobalAdmin(employeeId)) {
            return; // Global admins can create projects in any team
        }
        if (!isTeamMember(employeeId, teamId)) {
            throw new BadRequestException("You must be a team member to create projects");
        }
        if (!hasRoleInTeam(employeeId, teamId, "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can create projects");
        }
    }

    /**
     * Verify user can update/delete project
     * Allowed: Global ADMIN, or OWNER/MANAGER in team
     */
    public void verifyCanModifyProject(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Global admins can modify any project
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to modify projects");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER")) {
            throw new BadRequestException("Only OWNER or MANAGER can modify projects");
        }
    }

    /**
     * Verify user can assign employees to project
     * Allowed: Global ADMIN, or OWNER/MANAGER/TEAM_ADMIN in team
     */
    public void verifyCanAssignToProject(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to assign to projects");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can assign employees to projects");
        }
    }

    // ===================== TICKET PERMISSIONS =====================

    /**
     * Verify user can create ticket in project
     * User must be: Global admin OR (Team member AND Project member)
     */
    public void verifyCanCreateTicket(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Global admins can create tickets in any project
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to create tickets");
        }
        if (!isProjectMember(employeeId, projectId)) {
            throw new BadRequestException("You must be assigned to the project to create tickets");
        }
    }

    /**
     * Verify user can update ticket
     * Allowed: Global ADMIN, Ticket owner, OWNER/MANAGER in team
     */
    public void verifyCanUpdateTicket(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();

        // Global admins can update any ticket
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to update tickets");
        }

        boolean isOwner = ticket.getOwner().getId().equals(employeeId);
        boolean isManagerOrOwner = hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER");

        if (!isOwner && !isManagerOrOwner) {
            throw new BadRequestException("Only ticket owner, OWNER, or MANAGER can update tickets");
        }
    }

    /**
     * Verify user can update ticket status
     * Allowed: Global ADMIN, Ticket owner, MANAGER in team
     */
    public void verifyCanUpdateTicketStatus(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();

        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to update ticket status");
        }

        boolean isOwner = ticket.getOwner().getId().equals(employeeId);
        boolean isManager = hasRoleInTeam(employeeId, project.getTeam().getId(), "MANAGER");

        if (!isOwner && !isManager) {
            throw new BadRequestException("Only ticket owner or MANAGER can update ticket status");
        }
    }

    /**
     * Verify user can delete ticket (soft delete)
     * Allowed: Global ADMIN, OWNER/MANAGER in team
     */
    public void verifyCanDeleteTicket(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();

        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to delete tickets");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER")) {
            throw new BadRequestException("Only OWNER or MANAGER can delete tickets");
        }
    }

    // ===================== DAILY UPDATE PERMISSIONS =====================

    /**
     * Verify user can mention ticket in daily update
     * User must be: Global admin OR Project member
     */
    public void verifyCanMentionTicket(Long employeeId, Ticket ticket) {
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isProjectMember(employeeId, ticket.getProject().getId())) {
            throw new BadRequestException("You must be assigned to the project to mention tickets");
        }
        if (ticket.getDeletedAt() != null) {
            throw new BadRequestException("Cannot mention soft-deleted tickets");
        }
    }

    // ===================== TEAM MANAGEMENT PERMISSIONS =====================

    /**
     * Verify user can approve/reject join requests
     * Allowed: Global ADMIN, or OWNER/MANAGER/TEAM_ADMIN in team
     */
    public void verifyCanApproveJoinRequest(Long employeeId, Long teamId) {
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, teamId)) {
            throw new BadRequestException("You must be a team member to approve join requests");
        }
        if (!hasRoleInTeam(employeeId, teamId, "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can approve join requests");
        }
    }

    /**
     * Verify user can manage team members
     * Add/Remove: Global ADMIN, or OWNER/TEAM_ADMIN in team
     * Role assignment: Global ADMIN, or OWNER only in team
     */
    public void verifyCanManageTeamMembers(Long employeeId, Long teamId, boolean isRoleChange) {
        if (isGlobalAdmin(employeeId)) {
            return;
        }

        if (!isTeamMember(employeeId, teamId)) {
            throw new BadRequestException("You must be a team member to manage members");
        }
        if (isRoleChange) {
            if (!hasRoleInTeam(employeeId, teamId, "OWNER")) {
                throw new BadRequestException("Only OWNER can change member roles");
            }
        } else {
            if (!hasRoleInTeam(employeeId, teamId, "OWNER", "TEAM_ADMIN")) {
                throw new BadRequestException("Only OWNER or TEAM_ADMIN can manage team members");
            }
        }
    }

    /**
     * Verify team has at least one OWNER (before removing member)
     */
    public void verifyTeamHasOwner(Long teamId, Long employeeIdToRemove) {
        List<EmployeeTeamRole> members = employeeTeamRoleRepository.findByTeamId(teamId);
        long ownerCount = members.stream()
                .filter(etr -> "OWNER".equalsIgnoreCase(etr.getTeamRole().getName()))
                .count();

        Optional<EmployeeTeamRole> memberToRemove = members.stream()
                .filter(etr -> etr.getEmployee().getId().equals(employeeIdToRemove))
                .findFirst();

        if (memberToRemove.isPresent() && "OWNER".equalsIgnoreCase(memberToRemove.get().getTeamRole().getName())) {
            if (ownerCount <= 1) {
                throw new BadRequestException("Cannot remove the last OWNER from the team");
            }
        }
    }

    // ===================== PROJECT MEMBERSHIP CHECK =====================

    /**
     * Check if user is assigned to project
     */
    public boolean isProjectMember(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return project.getEmployees().stream()
                .anyMatch(emp -> emp.getId().equals(employeeId));
    }
}
