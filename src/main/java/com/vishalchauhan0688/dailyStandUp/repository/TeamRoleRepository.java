package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRoleRepository extends JpaRepository<TeamRole, Long>, JpaSpecificationExecutor<TeamRole> {

    @Query("SELECT r FROM TeamRole r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<TeamRole> searchByName(@Param("name") String name);

    Optional<TeamRole> findByName(String roleName);

    boolean existsByName(String roleName);

}
