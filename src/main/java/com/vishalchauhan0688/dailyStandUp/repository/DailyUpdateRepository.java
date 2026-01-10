package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long> {

}
