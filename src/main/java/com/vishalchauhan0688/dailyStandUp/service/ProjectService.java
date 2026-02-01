package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.ProjectUpdateDto;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import com.vishalchauhan0688.dailyStandUp.repository.ProjectRepository;
import com.vishalchauhan0688.dailyStandUp.util.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamService teamService;
    private final EmployeeService employeeService;
    private final AuthorizationService authorizationService;
    private final QueryService queryService;

    public List<Project> findAll() {
        return projectRepository.findAll().stream()
                .filter(p -> p.getDeletedAt() == null)
                .toList();
    }

    public PageResponse<Project> findAll(QueryParams params) {
        Function<String, Specification<Project>> searchSpecFactory = search -> {
            if (search == null || search.trim().isEmpty()) {
                return null;
            }

            String like = "%" + search.toLowerCase() + "%";

            return (root, query, cb) -> cb.and(
                    cb.isNull(root.get("deletedAt")),
                    cb.or(
                            cb.like(cb.lower(root.get("projectName")), like),
                            cb.like(cb.lower(root.get("projectDescription")), like)
                    )
            );
        };

        Page<Project> page = queryService.query(projectRepository, params, searchSpecFactory);

        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public Project findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found with id: " + id)
                );
        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Project has been deleted");
        }
        return project;
    }

    @Transactional
    public Project save(ProjectCreateDto dto) {
        Long employeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER, MANAGER, or TEAM_ADMIN can create projects
        authorizationService.verifyCanCreateProject(employeeId, dto.getTeamId());

        if (projectRepository.existsByProjectName(dto.getProjectName())) {
            throw new BadRequestException(
                    "Project already exists: " + dto.getProjectName()
            );
        }

        Team team = teamService.findById(dto.getTeamId());

        Project project = Project.builder()
                .projectName(dto.getProjectName())
                .projectDescription(dto.getProjectDescription())
                .team(team)
                .build();

        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, ProjectUpdateDto dto) {
        Long employeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER or MANAGER can update projects
        authorizationService.verifyCanModifyProject(employeeId, id);

        Project existing = findById(id);

        if (dto.getProjectName() != null && !existing.getProjectName().equals(dto.getProjectName())
                && projectRepository.existsByProjectName(dto.getProjectName())) {
            throw new BadRequestException(
                    "Project already exists: " + dto.getProjectName()
            );
        }

        if (dto.getProjectName() != null) {
            existing.setProjectName(dto.getProjectName());
        }

        if (dto.getProjectDescription() != null) {
            existing.setProjectDescription(dto.getProjectDescription());
        }

        return projectRepository.save(existing);
    }

    @Transactional
    public void addEmployeeToProject(Long projectId, Long employeeId) {
        Long currentEmployeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER, MANAGER, or TEAM_ADMIN can assign employees
        authorizationService.verifyCanAssignToProject(currentEmployeeId, projectId);

        Project project = findById(projectId);

        Employee employee = employeeService.findByIdEntity(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        // Verify employee is team member
        if (!authorizationService.isTeamMember(employeeId, project.getTeam().getId())) {
            throw new BadRequestException("Employee must be a team member to be assigned to project");
        }

        if (project.getEmployees().contains(employee)) {
            throw new BadRequestException("Employee is already assigned to this project");
        }

        project.getEmployees().add(employee);
        projectRepository.save(project);
    }

    @Transactional
    public void removeEmployeeFromProject(Long projectId, Long employeeId) {
        Long currentEmployeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER, MANAGER, or TEAM_ADMIN can remove employees
        authorizationService.verifyCanAssignToProject(currentEmployeeId, projectId);

        Project project = findById(projectId);

        Employee employee = employeeService.findByIdEntity(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        if (!project.getEmployees().contains(employee)) {
            throw new BadRequestException("Employee is not assigned to this project");
        }

        project.getEmployees().remove(employee);
        projectRepository.save(project);
    }

    @Transactional
    public void delete(Long id) {
        Long employeeId = employeeService.getMe().getId();
        
        // Authorization: Only OWNER or MANAGER can delete projects
        authorizationService.verifyCanModifyProject(employeeId, id);

        Project project = findById(id);
        
        // Soft delete - set deleted_at timestamp
        project.setDeletedAt(java.time.Instant.now());
        projectRepository.save(project);
    }
}
