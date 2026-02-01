package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.TeamCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TeamResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.TeamUpdateDto;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Team management.
 * Returns DTOs instead of entities to avoid lazy-loading issues.
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponseDto>>> getAll() {
        List<Team> teams = teamService.findAll();
        List<TeamResponseDto> dtos = teams.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Teams fetched successfully", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponseDto>> getById(@PathVariable Long id) {
        Team team = teamService.findById(id);
        TeamResponseDto dto = toResponseDto(team);
        return ResponseEntity.ok(ApiResponse.success("Team fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponseDto>> create(@Valid @RequestBody TeamCreateDto dto) {
        Team created = teamService.save(dto);
        TeamResponseDto responseDto = toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Team created successfully", responseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateDto dto) {
        Team updated = teamService.update(id, dto);
        TeamResponseDto responseDto = toResponseDto(updated);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", responseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }

    /**
     * Convert Team entity to TeamResponseDto.
     * Only includes essential fields to avoid N+1 queries.
     */
    private TeamResponseDto toResponseDto(Team team) {
        return TeamResponseDto.builder()
                .id(team.getId())
                .teamName(team.getTeamName())
                .description(team.getDescription())
                .memberCount(team.getEmployeeTeamRoles() != null ? team.getEmployeeTeamRoles().size() : 0)
                .projectCount(team.getProjects() != null ? team.getProjects().size() : 0)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
