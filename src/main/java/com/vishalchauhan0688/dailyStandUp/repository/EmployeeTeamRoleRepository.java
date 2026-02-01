package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.EmployeeTeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeTeamRoleRepository extends JpaRepository<EmployeeTeamRole, Long> {
    Optional<EmployeeTeamRole> findByEmployeeIdAndTeamId(Long employeeId, Long teamId);
    List<EmployeeTeamRole> findByTeamId(Long teamId);
    List<EmployeeTeamRole> findByEmployeeId(Long employeeId);
    boolean existsByEmployeeIdAndTeamId(Long employeeId, Long teamId);
}

