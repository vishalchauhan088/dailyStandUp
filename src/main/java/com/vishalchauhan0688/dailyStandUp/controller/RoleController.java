package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.repository.RoleRepository;
import com.vishalchauhan0688.dailyStandUp.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @GetMapping
    public List<Role> findAll() {
        return roleService.findAll();
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchByName(@Param("name") String name) {
        return roleService.searchByName(name).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public Role save(@RequestBody Role role) {
        return roleService.save(role);
    }

}
