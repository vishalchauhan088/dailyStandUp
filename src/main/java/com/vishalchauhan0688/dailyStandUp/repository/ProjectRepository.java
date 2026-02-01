package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    Optional<Project> findByProjectName(String projectName);

    boolean existsByProjectName(String projectName);

    List<Project> findByTeam(Team team);

    List<Project> findByTeamId(Long teamId);

    @Query("SELECT p FROM Project p JOIN p.employees e WHERE e.id = :employeeId")
    List<Project> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find all projects with their team eagerly loaded.
     * This prevents N+1 queries when accessing project.getTeam().
     */
    @EntityGraph(attributePaths = { "team" })
    List<Project> findAll();

    /**
     * Find project by ID with team eagerly loaded.
     * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithTeam" as a
     * property.
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.team WHERE p.id = :id")
    Optional<Project> findByIdWithTeam(@Param("id") Long id);

    /**
     * Find project by ID with all relations eagerly loaded.
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.team LEFT JOIN FETCH p.employees LEFT JOIN FETCH p.tickets WHERE p.id = :id")
    Optional<Project> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find projects by team ID with team eagerly loaded.
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.team WHERE p.team.id = :teamId")
    List<Project> findByTeamIdWithTeam(@Param("teamId") Long teamId);

    /**
     * Find projects by employee ID with team eagerly loaded.
     */
    @Query("SELECT DISTINCT p FROM Project p JOIN p.employees e LEFT JOIN FETCH p.team WHERE e.id = :employeeId")
    @EntityGraph(attributePaths = { "team" })
    List<Project> findByEmployeeIdWithTeam(@Param("employeeId") Long employeeId);

    /**
     * Find all projects with pagination and eager loading.
     */
    @Override
    @EntityGraph(attributePaths = { "team", "employees" })
    Page<Project> findAll(Pageable pageable);

    /**
     * Find non-deleted projects only.
     */
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL")
    @EntityGraph(attributePaths = { "team" })
    List<Project> findAllActive();

    /**
     * Find non-deleted projects with pagination.
     */
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL")
    @EntityGraph(attributePaths = { "team", "employees" })
    Page<Project> findAllActive(Pageable pageable);
}
