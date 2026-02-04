package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.TeamRole;
import com.vishalchauhan0688.dailyStandUp.repository.TeamRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRoleRepository teamRoleRepository;

    public List<TeamRole> findAll() {
        return teamRoleRepository.findAll();
    }

    public TeamRole findById(Long id) {
        return teamRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    public List<TeamRole> searchByName(String name) {
        return teamRoleRepository.searchByName(name);
    }

    @Transactional
    public TeamRole save(TeamRole teamRole) {
        if (teamRole.getName() == null || teamRole.getName().trim().isEmpty()) {
            throw new BadRequestException("Role name is required");
        }
        if (teamRoleRepository.existsByName(teamRole.getName())) {
            throw new BadRequestException("Role already exists: " + teamRole.getName());
        }
        return teamRoleRepository.save(teamRole);
    }

    @Transactional
    public TeamRole update(Long id, TeamRole teamRole) {
        TeamRole existing = findById(id);
        if (!existing.getName().equals(teamRole.getName()) && teamRoleRepository.existsByName(teamRole.getName())) {
            throw new BadRequestException("Role already exists: " + teamRole.getName());
        }
        existing.setName(teamRole.getName());
        return teamRoleRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        TeamRole teamRole = findById(id);
        teamRoleRepository.delete(teamRole);
    }
}
