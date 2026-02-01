package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    public List<Role> searchByName(String name) {
        return roleRepository.searchByName(name);
    }

    @Transactional
    public Role save(Role role) {
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new BadRequestException("Role name is required");
        }
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new BadRequestException("Role already exists: " + role.getRoleName());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, Role role) {
        Role existing = findById(id);
        if (!existing.getRoleName().equals(role.getRoleName()) && roleRepository.existsByRoleName(role.getRoleName())) {
            throw new BadRequestException("Role already exists: " + role.getRoleName());
        }
        existing.setRoleName(role.getRoleName());
        return roleRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Role role = findById(id);
        roleRepository.delete(role);
    }
}
