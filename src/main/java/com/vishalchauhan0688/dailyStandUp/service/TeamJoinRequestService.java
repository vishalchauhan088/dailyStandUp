package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.model.TeamJoinRequest;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TeamJoinRequestRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamJoinRequestService {

    private final TeamJoinRequestRepository teamJoinRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public TeamJoinRequest createJoinRequest(Long employeeId, Long teamId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if already a member
        if (employeeTeamRoleRepository.existsByEmployeeIdAndTeamId(employeeId, teamId)) {
            throw new BadRequestException("Employee is already a member of this team");
        }

        // Check if request already exists
        teamJoinRequestRepository.findByEmployeeIdAndTeamId(employeeId, teamId)
                .ifPresent(request -> {
                    if ("PENDING".equalsIgnoreCase(request.getStatus())) {
                        throw new BadRequestException("Join request already pending for this team");
                    }
                });

        TeamJoinRequest request = TeamJoinRequest.builder()
                .employee(employee)
                .team(team)
                .status("PENDING")
                .build();

        return teamJoinRequestRepository.save(request);
    }

    @Transactional
    public TeamJoinRequest approveRequest(Long requestId, Long approverId) {
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found with id: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Request is not pending");
        }

        // Verify approver has permission (OWNER, MANAGER, or TEAM_ADMIN)
        EmployeeTeamRole approverRole = employeeTeamRoleRepository
                .findByEmployeeIdAndTeamId(approverId, request.getTeam().getId())
                .orElseThrow(() -> new BadRequestException("Approver is not a member of this team"));

        String roleName = approverRole.getRole().getRoleName();
        if (!"OWNER".equalsIgnoreCase(roleName) && 
            !"MANAGER".equalsIgnoreCase(roleName) && 
            !"TEAM_ADMIN".equalsIgnoreCase(roleName)) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can approve join requests");
        }

        request.setStatus("APPROVED");
        teamJoinRequestRepository.save(request);

        // Add employee to team with MEMBER role
        Role memberRole = roleRepository.findByRoleName("MEMBER")
                .orElseThrow(() -> new ResourceNotFoundException("MEMBER role not found"));

        EmployeeTeamRole employeeTeamRole = EmployeeTeamRole.builder()
                .employee(request.getEmployee())
                .team(request.getTeam())
                .role(memberRole)
                .build();

        employeeTeamRoleRepository.save(employeeTeamRole);

        return request;
    }

    @Transactional
    public TeamJoinRequest rejectRequest(Long requestId, Long approverId) {
        TeamJoinRequest request = teamJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found with id: " + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Request is not pending");
        }

        // Verify approver has permission
        EmployeeTeamRole approverRole = employeeTeamRoleRepository
                .findByEmployeeIdAndTeamId(approverId, request.getTeam().getId())
                .orElseThrow(() -> new BadRequestException("Approver is not a member of this team"));

        String roleName = approverRole.getRole().getRoleName();
        if (!"OWNER".equalsIgnoreCase(roleName) && 
            !"MANAGER".equalsIgnoreCase(roleName) && 
            !"TEAM_ADMIN".equalsIgnoreCase(roleName)) {
            throw new BadRequestException("Only OWNER, MANAGER, or TEAM_ADMIN can reject join requests");
        }

        request.setStatus("REJECTED");
        return teamJoinRequestRepository.save(request);
    }

    public List<TeamJoinRequest> getPendingRequestsForTeam(Long teamId) {
        return teamJoinRequestRepository.findByTeamIdAndStatus(teamId, "PENDING");
    }

    public List<TeamJoinRequest> getRequestsByEmployee(Long employeeId) {
        return teamJoinRequestRepository.findByEmployeeId(employeeId);
    }
}

