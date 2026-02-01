package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long>, JpaSpecificationExecutor<DailyUpdate> {
    List<DailyUpdate> findByEmployeeId(Long employeeId);
}
