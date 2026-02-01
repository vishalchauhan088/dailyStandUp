package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Employee;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

        Optional<Employee> findByEmail(String email);

        Optional<Employee> findByUsername(String username);

        boolean existsByEmail(String email);

        boolean existsByUsername(String username);

        /**
         * Find employee by ID with team roles and global roles eagerly loaded.
         * This prevents N+1 queries when accessing employee.getTeamRoles().
         * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithRoles" as a
         * property.
         */
        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.teamRoles tr LEFT JOIN FETCH tr.team LEFT JOIN FETCH tr.role LEFT JOIN FETCH e.globalRoles gr LEFT JOIN FETCH gr.globalRole WHERE e.id = :id")
        @EntityGraph(attributePaths = { "teamRoles", "teamRoles.team", "teamRoles.role", "globalRoles",
                        "globalRoles.globalRole" })
        Optional<Employee> findByIdWithRoles(@Param("id") Long id);

        /**
         * Find employee by email with team roles and global roles eagerly loaded.
         * NOTE: Using @Query to avoid Spring Data JPA interpreting "WithRoles" as a
         * property.
         */
        @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.teamRoles tr LEFT JOIN FETCH tr.team LEFT JOIN FETCH tr.role LEFT JOIN FETCH e.globalRoles gr LEFT JOIN FETCH gr.globalRole WHERE e.email = :email")
        @EntityGraph(attributePaths = { "teamRoles", "teamRoles.team", "teamRoles.role", "globalRoles",
                        "globalRoles.globalRole" })
        Optional<Employee> findByEmailWithRoles(@Param("email") String email);

        /**
         * Find all employees with their team roles eagerly loaded.
         */
        @EntityGraph(attributePaths = { "teamRoles", "teamRoles.team", "teamRoles.role" })
        List<Employee> findAll();

        /**
         * Find all employees with pagination.
         */
        @Override
        @EntityGraph(attributePaths = { "teamRoles", "teamRoles.team", "teamRoles.role", "globalRoles" })
        Page<Employee> findAll(Pageable pageable);

        /**
         * Check if employee has global ADMIN role.
         */
        @Query("SELECT COUNT(egr) > 0 FROM EmployeeGlobalRole egr " +
                        "JOIN egr.globalRole gr " +
                        "WHERE egr.employee.id = :employeeId AND gr.name = 'ADMIN'")
        boolean isGlobalAdmin(@Param("employeeId") Long employeeId);

        /**
         * Check if employee has any global role.
         */
        @Query("SELECT COUNT(egr) > 0 FROM EmployeeGlobalRole egr " +
                        "WHERE egr.employee.id = :employeeId")
        boolean hasAnyGlobalRole(@Param("employeeId") Long employeeId);

        @Modifying
        @Transactional
        @Query("Delete from Employee e where e.id = :id")
        int deleteEmployeeById(@Param("id") Long id);

}
