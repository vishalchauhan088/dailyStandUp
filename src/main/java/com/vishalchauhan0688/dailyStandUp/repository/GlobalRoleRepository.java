package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.GlobalRole;
import com.vishalchauhan0688.dailyStandUp.model.GlobalRole.GlobalRoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalRoleRepository extends JpaRepository<GlobalRole, Long> {

    Optional<GlobalRole> findByName(GlobalRoleName name);

    boolean existsByName(GlobalRoleName name);

    /**
     * Find all global roles for a specific employee
     */
    @Query("SELECT gr FROM GlobalRole gr JOIN EmployeeGlobalRole egr ON gr = egr.globalRole " +
            "WHERE egr.employee.id = :employeeId")
    List<GlobalRole> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Check if employee has global ADMIN role
     */
    @Query("SELECT COUNT(gr) > 0 FROM GlobalRole gr " +
            "JOIN EmployeeGlobalRole egr ON gr = egr.globalRole " +
            "WHERE egr.employee.id = :employeeId AND gr.name = 'ADMIN'")
    boolean isGlobalAdmin(@Param("employeeId") Long employeeId);

    /**
     * Check if employee has any global role
     */
    @Query("SELECT COUNT(egr) > 0 FROM EmployeeGlobalRole egr " +
            "WHERE egr.employee.id = :employeeId")
    boolean hasAnyGlobalRole(@Param("employeeId") Long employeeId);
}
