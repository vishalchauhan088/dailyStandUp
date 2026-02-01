package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.*;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Centralized authorization service for RBAC checks
 * All authorization logic is enforced here
 */
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;
    private final ProjectRepository projectRepository;

    /**
     * Check if user is global ADMIN
     */
    public boolean isGlobalAdmin(Long employeeId) {
        List<EmployeeTeamRole> roles = employeeTeamRoleRepository.findByEmployeeId(employeeId);
        return roles.stream()
                .anyMatch(etr -> "ADMIN".equalsIgnoreCase(etr.getRole().getRoleName()));
    }

    /**
     * Check if user has specific role in team
     */
    public boolean hasRoleInTeam(Long employeeId, Long teamId, String... roleNames) {
        Optional<EmployeeTeamRole> teamRole = employeeTeamRoleRepository.findByEmployeeIdAndTeamId(employeeId, teamId);
        if (teamRole.isEmpty()) {
            return false;
        }
        String userRole = teamRole.get().getRole().getRoleName();
        for (String roleName : roleNames) {
            if (roleName.equalsIgnoreCase(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is team member
     */
    public boolean isTeamMember(Long employeeId, Long teamId) {
        return employeeTeamRoleRepository.existsByEmployeeIdAndTeamId(employeeId, teamId);
    }

    /**
     * Check if user is assigned to project
     */
    public boolean isProjectMember(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return project.getEmployees().stream()
                .anyMatch(emp -> emp.getId().equals(employeeId));
    }

    /**
     * Get user's role in team
     */
    public String getRoleInTeam(Long employeeId, Long teamId) {
        return employeeTeamRoleRepository.findByEmployeeIdAndTeamId(employeeId, teamId)
                .map(etr -> etr.getRole().getRoleName())
                .orElse(null);
    }

    /**
     * Verify user can create project in team
     * Allowed: OWNER, MANAGER, TEAM_ADMIN
     */
    public void verifyCanCreateProject(Long employeeId, Long teamId) {
        if (!isTeamMember(employeeId, teamId)) {
            throw new BadRequestException("You must be a team member to create projects");
        }
        if (!hasRoleInTeam(employeeId, teamId, "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can create projects");
        }
    }

    /**
     * Verify user can update/delete project
     * Allowed: OWNER, MANAGER
     */
    public void verifyCanModifyProject(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to modify projects");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER")) {
            throw new BadRequestException("Only OWNER or MANAGER can modify projects");
        }
    }

    /**
     * Verify user can assign employees to project
     * Allowed: OWNER, MANAGER, TEAM_ADMIN
     */
    public void verifyCanAssignToProject(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to assign to projects");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can assign employees to projects");
        }
    }

    /**
     * Verify user can create ticket in project
     * User must be: Team member AND Project member
     */
    public void verifyCanCreateTicket(Long employeeId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to create tickets");
        }
        if (!isProjectMember(employeeId, projectId)) {
            throw new BadRequestException("You must be assigned to the project to create tickets");
        }
    }

    /**
     * Verify user can update ticket
     * Allowed: Ticket owner, OWNER, MANAGER
     */
    public void verifyCanUpdateTicket(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();
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
     * Allowed: Ticket owner, MANAGER
     */
    public void verifyCanUpdateTicketStatus(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();
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
     * Allowed: OWNER, MANAGER
     */
    public void verifyCanDeleteTicket(Long employeeId, Ticket ticket) {
        Project project = ticket.getProject();
        if (!isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("You must be a team member to delete tickets");
        }
        if (!hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER")) {
            throw new BadRequestException("Only OWNER or MANAGER can delete tickets");
        }
    }

    /**
     * Verify user can mention ticket in daily update
     * User must be: Project member AND Ticket belongs to assigned project
     */
    public void verifyCanMentionTicket(Long employeeId, Ticket ticket) {
        if (!isProjectMember(employeeId, ticket.getProject().getId())) {
            throw new BadRequestException("You must be assigned to the project to mention tickets");
        }
        if (ticket.getDeletedAt() != null) {
            throw new BadRequestException("Cannot mention soft-deleted tickets");
        }
    }

    /**
     * Verify user can approve/reject join requests
     * Allowed: OWNER, MANAGER, TEAM_ADMIN
     */
    public void verifyCanApproveJoinRequest(Long employeeId, Long teamId) {
        if (!isTeamMember(employeeId, teamId)) {
            throw new BadRequestException("You must be a team member to approve join requests");
        }
        if (!hasRoleInTeam(employeeId, teamId, "OWNER", "MANAGER", "TEAM_ADMIN")) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can approve join requests");
        }
    }

    /**
     * Verify user can manage team members
     * Add/Remove: OWNER, TEAM_ADMIN
     * Role assignment: OWNER only
     */
    public void verifyCanManageTeamMembers(Long employeeId, Long teamId, boolean isRoleChange) {
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
                .filter(etr -> "OWNER".equalsIgnoreCase(etr.getRole().getRoleName()))
                .count();
        
        Optional<EmployeeTeamRole> memberToRemove = members.stream()
                .filter(etr -> etr.getEmployee().getId().equals(employeeIdToRemove))
                .findFirst();
        
        if (memberToRemove.isPresent() && "OWNER".equalsIgnoreCase(memberToRemove.get().getRole().getRoleName())) {
            if (ownerCount <= 1) {
                throw new BadRequestException("Cannot remove the last OWNER from the team");
            }
        }
    }
}

