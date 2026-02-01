package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeRepository;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeTeamRoleService {

    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;

    public List<EmployeeTeamRole> findByTeamId(Long teamId) {
        return employeeTeamRoleRepository.findByTeamId(teamId);
    }

    public List<EmployeeTeamRole> findByEmployeeId(Long employeeId) {
        return employeeTeamRoleRepository.findByEmployeeId(employeeId);
    }

    public EmployeeTeamRole findByEmployeeIdAndTeamId(Long employeeId, Long teamId) {
        return employeeTeamRoleRepository.findByEmployeeIdAndTeamId(employeeId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee is not a member of team with id: " + teamId));
    }

    @Transactional
    public EmployeeTeamRole addEmployeeToTeam(Long employeeId, Long teamId, Long roleId) {
        if (employeeTeamRoleRepository.existsByEmployeeIdAndTeamId(employeeId, teamId)) {
            throw new BadRequestException("Employee is already a member of this team");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        EmployeeTeamRole employeeTeamRole = EmployeeTeamRole.builder()
                .employee(employee)
                .team(team)
                .role(role)
                .build();

        return employeeTeamRoleRepository.save(employeeTeamRole);
    }

    @Transactional
    public void updateEmployeeRoleInTeam(Long employeeId, Long teamId, Long roleId) {
        EmployeeTeamRole employeeTeamRole = findByEmployeeIdAndTeamId(employeeId, teamId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        employeeTeamRole.setRole(role);
        employeeTeamRoleRepository.save(employeeTeamRole);
    }

    @Transactional
    public void removeEmployeeFromTeam(Long employeeId, Long teamId) {
        EmployeeTeamRole employeeTeamRole = findByEmployeeIdAndTeamId(employeeId, teamId);
        
        // Check if this is the last OWNER
        if ("OWNER".equalsIgnoreCase(employeeTeamRole.getRole().getRoleName())) {
            long ownerCount = employeeTeamRoleRepository.findByTeamId(teamId).stream()
                    .filter(etr -> "OWNER".equalsIgnoreCase(etr.getRole().getRoleName()))
                    .count();
            if (ownerCount <= 1) {
                throw new BadRequestException("Cannot remove the last OWNER from the team");
            }
        }

        employeeTeamRoleRepository.delete(employeeTeamRole);
    }
}

