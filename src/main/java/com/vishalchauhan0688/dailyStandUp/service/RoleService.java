package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    public List<Role> findAll(){
        return roleRepository.findAll();
    }
    public Optional<Role> searchByName(String name){
        return roleRepository.findByName(name);
    }
    public Role findById(Long id){
        return roleRepository.findById(id).orElse(null);
    }
    public Role save(Role role){
        if(!role.getName().startsWith("ROLE_")){
            throw new IllegalArgumentException("Role name cannot start with 'ROLE_'");
        }
        return roleRepository.save(role);
    }
}
