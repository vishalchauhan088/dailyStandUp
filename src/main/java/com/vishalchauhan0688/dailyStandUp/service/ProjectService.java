package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
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
    private final QueryService queryService;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public PageResponse<Project> findAll(QueryParams params) {

        Function<String, Specification<Project>> searchSpecFactory = search -> {
            if (search == null || search.trim().isEmpty()) {
                return null;
            }

            String like = "%" + search.toLowerCase() + "%";

            return (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("projectName")), like),
                    cb.like(cb.lower(root.get("projectDescription")), like)
            );
        };

        Page<Project> page =
                queryService.query(projectRepository, params, searchSpecFactory);

        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found with id: " + id)
                );
    }

    @Transactional
    public Project save(Project project) {
        if (projectRepository.existsByProjectName(project.getProjectName())) {
            throw new BadRequestException(
                    "Project already exists: " + project.getProjectName()
            );
        }

        if (project.getTeam() == null || project.getTeam().getId() == null) {
            throw new BadRequestException("Team is required for project");
        }

        Team team = teamService.findById(project.getTeam().getId());
        project.setTeam(team);

        return projectRepository.save(project);
    }

    @Transactional
    public Project update(Long id, Project project) {
        Project existing = findById(id);

        if (!existing.getProjectName().equals(project.getProjectName())
                && projectRepository.existsByProjectName(project.getProjectName())) {
            throw new BadRequestException(
                    "Project already exists: " + project.getProjectName()
            );
        }

        existing.setProjectName(project.getProjectName());

        if (project.getProjectDescription() != null) {
            existing.setProjectDescription(project.getProjectDescription());
        }

        if (project.getTeam() != null && project.getTeam().getId() != null) {
            Team team = teamService.findById(project.getTeam().getId());
            existing.setTeam(team);
        }

        return projectRepository.save(existing);
    }

    @Transactional
    public void addEmployeeToProject(Long projectId, Long employeeId) {
        Project project = findById(projectId);

        Employee employee = employeeService.findByIdEntity(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        project.getEmployees().add(employee);
        projectRepository.save(project);
    }

    @Transactional
    public void removeEmployeeFromProject(Long projectId, Long employeeId) {
        Project project = findById(projectId);

        Employee employee = employeeService.findByIdEntity(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        project.getEmployees().remove(employee);
        projectRepository.save(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = findById(id);
        
        // Soft delete - set deleted_at timestamp
        project.setDeletedAt(java.time.Instant.now());
        projectRepository.save(project);
    }
}
