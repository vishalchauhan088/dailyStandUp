package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> findAll() {
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getById(@PathVariable Long id) {
        Role role = roleService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Role fetched successfully", role));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> save(@Valid @RequestBody Role role) {
        Role created = roleService.save(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Role created successfully", created));
    }
}
