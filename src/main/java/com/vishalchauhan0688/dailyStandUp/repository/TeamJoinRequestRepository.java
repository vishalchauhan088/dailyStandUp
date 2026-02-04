package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.TeamJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {
    Optional<TeamJoinRequest> findByEmployeeIdAndTeamId(Long employeeId, Long teamId);
    List<TeamJoinRequest> findByTeamId(Long teamId);
    List<TeamJoinRequest> findByEmployeeId(Long employeeId);
    List<TeamJoinRequest> findByTeamIdAndStatus(Long teamId, String status);
}

