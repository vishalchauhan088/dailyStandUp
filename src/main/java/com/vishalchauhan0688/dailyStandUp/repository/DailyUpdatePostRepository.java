package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyUpdatePostRepository
        extends JpaRepository<DailyUpdatePost, Long>, JpaSpecificationExecutor<DailyUpdatePost> {

    Optional<DailyUpdatePost> findByEmployeeIdAndTeamIdAndDate(Long employeeId, Long teamId, LocalDate date);

    List<DailyUpdatePost> findByTeamIdAndDate(Long teamId, LocalDate date);

    List<DailyUpdatePost> findByEmployeeIdAndTeamId(Long employeeId, Long teamId);

    List<DailyUpdatePost> findByTeamId(Long teamId);

    /**
     * Find daily updates by date with employee and team eagerly loaded.
     * Prevents N+1 query issues when accessing employee/team data.
     */
    @EntityGraph(attributePaths = { "employee", "team" })
    List<DailyUpdatePost> findByDate(LocalDate date);

    /**
     * Find all daily updates with employee and team eagerly loaded.
     */
    @EntityGraph(attributePaths = { "employee", "team" })
    List<DailyUpdatePost> findAll();

    /**
     * Find daily update by ID with all relations eagerly loaded.
     * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithAllRelations"
     * as a property.
     */
    @Query("SELECT d FROM DailyUpdatePost d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.team LEFT JOIN FETCH d.ticketMentions tm LEFT JOIN FETCH tm.ticket WHERE d.id = :id")
    Optional<DailyUpdatePost> findByIdWithAllRelations(@Param("id") Long id);

    /**
     * Find daily updates by team and date with employee eagerly loaded.
     * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithEmployee" as a
     * property.
     */
    @Query("SELECT d FROM DailyUpdatePost d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.team WHERE d.team.id = :teamId AND d.date = :date")
    @EntityGraph(attributePaths = { "employee", "team" })
    List<DailyUpdatePost> findByTeamIdAndDateWithEmployee(@Param("teamId") Long teamId, @Param("date") LocalDate date);

    /**
     * Find daily updates by employee with team eagerly loaded.
     * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithTeam" as a
     * property.
     */
    @Query("SELECT d FROM DailyUpdatePost d LEFT JOIN FETCH d.employee LEFT JOIN FETCH d.team WHERE d.employee.id = :employeeId")
    @EntityGraph(attributePaths = { "employee", "team" })
    List<DailyUpdatePost> findByEmployeeIdWithTeam(@Param("employeeId") Long employeeId);

    /**
     * Find all daily updates with pagination.
     */
    @Override
    @EntityGraph(attributePaths = { "employee", "team" })
    Page<DailyUpdatePost> findAll(Pageable pageable);
}
