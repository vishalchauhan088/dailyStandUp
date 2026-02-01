package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyUpdatePostRepository extends JpaRepository<DailyUpdatePost, Long>, JpaSpecificationExecutor<DailyUpdatePost> {
    Optional<DailyUpdatePost> findByEmployeeIdAndTeamIdAndDate(Long employeeId, Long teamId, LocalDate date);
    List<DailyUpdatePost> findByTeamIdAndDate(Long teamId, LocalDate date);
    List<DailyUpdatePost> findByEmployeeIdAndTeamId(Long employeeId, Long teamId);
    List<DailyUpdatePost> findByTeamId(Long teamId);
    List<DailyUpdatePost> findByDate(LocalDate date);
}

