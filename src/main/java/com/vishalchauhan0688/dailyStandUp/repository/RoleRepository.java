package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Role.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @Query("SELECT r FROM Role r WHERE LOWER(r.roleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Role> searchByName(@Param("name") String name);

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    /**
     * Find all system roles (global permissions)
     */
    List<Role> findByRoleType(RoleType roleType);

    /**
     * Find all team-specific roles
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'TEAM'")
    List<Role> findAllTeamRoles();

    /**
     * Find all system/global roles
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'SYSTEM'")
    List<Role> findAllSystemRoles();

    /**
     * Check if a role is a system role
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Role r WHERE r.roleName = :roleName AND r.roleType = 'SYSTEM'")
    boolean isSystemRole(@Param("roleName") String roleName);

    /**
     * Check if a role is a team role
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Role r WHERE r.roleName = :roleName AND r.roleType = 'TEAM'")
    boolean isTeamRole(@Param("roleName") String roleName);
}
