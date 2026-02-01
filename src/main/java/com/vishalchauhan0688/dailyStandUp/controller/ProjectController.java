package com.vishalchauhan0688.dailyStandUp.controller;

import com.vishalchauhan0688.dailyStandUp.dto.ApiResponse;
import com.vishalchauhan0688.dailyStandUp.dto.AssignEmployeeToProjectDto;
import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectUpdateDto;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getById(@PathVariable Long id) {
        Project project = projectService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully", project));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Project>> create(@Valid @RequestBody ProjectCreateDto dto) {
        Project created = projectService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Project created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> update(
            @PathVariable Long id, 
            @Valid @RequestBody ProjectUpdateDto dto) {
        Project updated = projectService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", updated));
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
}
