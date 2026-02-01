package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.TeamCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TeamUpdateDto;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.repository.EmployeeTeamRoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final EmployeeService employeeService;
    private final RoleRepository roleRepository;
    private final EmployeeTeamRoleRepository employeeTeamRoleRepository;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Team findById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    public Team findByTeamName(String teamName) {
        return teamRepository.findByTeamName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamName));
    }

    @Transactional
    public Team save(TeamCreateDto dto) {
        if (teamRepository.existsByTeamName(dto.getTeamName())) {
            throw new BadRequestException("Team already exists: " + dto.getTeamName());
        }

        Team team = Team.builder()
                .teamName(dto.getTeamName())
                .description(dto.getDescription())
                .build();
        
        team = teamRepository.save(team);

        // Creator becomes OWNER
        Employee creator = employeeService.getMe();
        Role ownerRole = roleRepository.findByRoleName("OWNER")
                .orElseThrow(() -> new ResourceNotFoundException("OWNER role not found"));

        EmployeeTeamRole employeeTeamRole = EmployeeTeamRole.builder()
                .employee(creator)
                .team(team)
                .role(ownerRole)
                .build();

        employeeTeamRoleRepository.save(employeeTeamRole);

        return team;
    }

    @Transactional
    public Team update(Long id, TeamUpdateDto dto) {
        Team existing = findById(id);
        
        if (dto.getTeamName() != null && !existing.getTeamName().equals(dto.getTeamName()) && 
            teamRepository.existsByTeamName(dto.getTeamName())) {
            throw new BadRequestException("Team already exists: " + dto.getTeamName());
        }
        
        if (dto.getTeamName() != null) {
            existing.setTeamName(dto.getTeamName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        return teamRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Team team = findById(id);
        // Check if team has members (through EmployeeTeamRole)
        if (!team.getEmployeeTeamRoles().isEmpty()) {
            throw new BadRequestException("Cannot delete team with existing members");
        }
        // Check if team has projects
        if (!team.getProjects().isEmpty()) {
            throw new BadRequestException("Cannot delete team with existing projects");
        }
        teamRepository.delete(team);
    }
}
