package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.model.Team;
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
}
