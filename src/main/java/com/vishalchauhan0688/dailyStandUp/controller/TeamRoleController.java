package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.RoleResponseDto;
import com.vishalchauhan0688.dailyStandUp.model.TeamRole;
import com.vishalchauhan0688.dailyStandUp.service.TeamRoleService;
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
public class TeamRoleController {
    private final TeamRoleService teamRoleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> findAll() {
        List<TeamRole> teamRoles = teamRoleService.findAll();
        List<RoleResponseDto> dtos = teamRoles.stream()
                .map(RoleResponseDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles fetched successfully", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getById(@PathVariable Long id) {
        TeamRole teamRole = teamRoleService.findById(id);
        RoleResponseDto dto = RoleResponseDto.fromEntity(teamRole);
        return ResponseEntity.ok(ApiResponse.success("Role fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> save(@Valid @RequestBody TeamRole teamRole) {
        TeamRole created = teamRoleService.save(teamRole);
        RoleResponseDto dto = RoleResponseDto.fromEntity(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Role created successfully", dto));
    }
}
