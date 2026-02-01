package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {
    Optional<Team> findByTeamName(String teamName);
    boolean existsByTeamName(String teamName);
}
