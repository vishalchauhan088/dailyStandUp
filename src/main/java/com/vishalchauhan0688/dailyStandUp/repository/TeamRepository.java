package com.vishalchauhan0688.dailyStandUp.repository;

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
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    Optional<Team> findByTeamName(String teamName);

    boolean existsByTeamName(String teamName);

    /**
     * Find all teams with their employee roles eagerly loaded.
     * This prevents N+1 queries when accessing team.getEmployeeTeamRoles().
     */
    @EntityGraph(attributePaths = { "employeeTeamRoles", "employeeTeamRoles.employee", "employeeTeamRoles.teamRole" })
    List<Team> findAll();

    /**
     * Find team by ID with employee roles eagerly loaded.
     * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithEmployeeRoles"
     * as a property.
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.employeeTeamRoles etr LEFT JOIN FETCH etr.employee LEFT JOIN FETCH etr.teamRole WHERE t.id = :id")
    @EntityGraph(attributePaths = { "employeeTeamRoles", "employeeTeamRoles.employee", "employeeTeamRoles.teamRole" })
    Optional<Team> findByIdWithEmployeeRoles(@Param("id") Long id);

    /**
     * Find team by ID with both employee roles and projects.
     */
    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.employeeTeamRoles etr LEFT JOIN FETCH etr.employee LEFT JOIN FETCH etr.teamRole LEFT JOIN FETCH t.projects WHERE t.id = :id")
    @EntityGraph(attributePaths = { "employeeTeamRoles", "employeeTeamRoles.employee", "projects" })
    Optional<Team> findByIdWithAllRelations(Long id);

    /**
     * Find teams by team name pattern (case-insensitive).
     */
    @Query("SELECT t FROM Team t WHERE LOWER(t.teamName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Team> searchByTeamName(@Param("name") String name);

    /**
     * Find all teams with pagination.
     */
    @Override
    @EntityGraph(attributePaths = { "employeeTeamRoles", "projects" })
    Page<Team> findAll(Pageable pageable);

    /**
     * Find teams where the employee is a member.
     */
    @Query("SELECT DISTINCT t FROM Team t JOIN t.employeeTeamRoles etr WHERE etr.employee.id = :employeeId")
    @EntityGraph(attributePaths = { "employeeTeamRoles", "projects" })
    List<Team> findByEmployeeId(@Param("employeeId") Long employeeId);
}
