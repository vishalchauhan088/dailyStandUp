package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.RoleResponseDto;
import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Role management.
 * Returns DTOs instead of entities to avoid lazy-loading issues.
 */
@RestController
@RequestMapping("/api/v1/public/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> findAll() {
        List<Role> roles = roleService.findAll();
        List<RoleResponseDto> dtos = roles.stream()
                .map(RoleResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getById(@PathVariable Long id) {
        Role role = roleService.findById(id);
        RoleResponseDto dto = RoleResponseDto.fromEntity(role);
        return ResponseEntity.ok(ApiResponse.success("Role fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> save(@Valid @RequestBody Role role) {
        Role created = roleService.save(role);
        RoleResponseDto dto = RoleResponseDto.fromEntity(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Role created successfully", dto));
    }
}
