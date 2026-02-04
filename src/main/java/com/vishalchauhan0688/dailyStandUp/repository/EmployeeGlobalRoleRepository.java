package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.EmployeeGlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeGlobalRoleRepository extends JpaRepository<EmployeeGlobalRole, Long> {

    Optional<EmployeeGlobalRole> findByEmployeeIdAndGlobalRoleId(Long employeeId, Long globalRoleId);

    List<EmployeeGlobalRole> findByEmployeeId(Long employeeId);

    List<EmployeeGlobalRole> findByGlobalRoleId(Long globalRoleId);

    boolean existsByEmployeeIdAndGlobalRoleId(Long employeeId, Long globalRoleId);

    /**
     * Delete all global role assignments for an employee
     */
    @Query("DELETE FROM EmployeeGlobalRole egr WHERE egr.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
