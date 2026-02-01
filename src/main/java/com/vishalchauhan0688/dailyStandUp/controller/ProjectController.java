package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.AssignEmployeeToProjectDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectResponseDto;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectUpdateDto;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Project management.
 * Returns DTOs instead of entities to avoid lazy-loading issues.
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter) {

        QueryParams params = QueryParams.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sort(sort)
                .search(search)
                .filter(filter)
                .build();

        PageResponse<Project> result = projectService.findAll(params);

        // Convert to DTOs
        List<ProjectResponseDto> dtos = result.getContent().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        PageResponse<ProjectResponseDto> dtoResponse = PageResponse.of(
                dtos, result.getPage(), result.getSize(), result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully", dtoResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        ProjectResponseDto dto = toResponseDto(project);
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponseDto>> create(@Valid @RequestBody ProjectCreateDto dto) {
        Project created = projectService.save(dto);
        ProjectResponseDto responseDto = toResponseDto(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Project created successfully", responseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateDto dto) {
        Project updated = projectService.update(id, dto);
        ProjectResponseDto responseDto = toResponseDto(updated);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", responseDto));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AssignEmployeeToProjectDto dto) {
        projectService.addEmployeeToProject(id, dto.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("Employee added to project successfully", null));
    }

    @DeleteMapping("/{id}/members/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long employeeId) {
        projectService.removeEmployeeFromProject(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee removed from project successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    /**
     * Convert Project entity to ProjectResponseDto.
     * Only includes essential fields to avoid N+1 queries.
     */
    private ProjectResponseDto toResponseDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .teamId(project.getTeam() != null ? project.getTeam().getId() : null)
                .teamName(project.getTeam() != null ? project.getTeam().getTeamName() : null)
                .ticketCount(project.getTickets() != null ? project.getTickets().size() : 0)
                .memberCount(project.getEmployees() != null ? project.getEmployees().size() : 0)
                .deletedAt(project.getDeletedAt())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
