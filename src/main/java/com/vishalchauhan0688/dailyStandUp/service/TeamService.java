package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;

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
    public Team save(Team team) {
        if (teamRepository.existsByTeamName(team.getTeamName())) {
            throw new BadRequestException("Team already exists: " + team.getTeamName());
        }
        return teamRepository.save(team);
    }

    @Transactional
    public Team update(Long id, Team team) {
        Team existing = findById(id);
        if (!existing.getTeamName().equals(team.getTeamName()) && 
            teamRepository.existsByTeamName(team.getTeamName())) {
            throw new BadRequestException("Team already exists: " + team.getTeamName());
        }
        existing.setTeamName(team.getTeamName());
        if (team.getDescription() != null) {
            existing.setDescription(team.getDescription());
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
        teamRepository.delete(team);
    }
}
