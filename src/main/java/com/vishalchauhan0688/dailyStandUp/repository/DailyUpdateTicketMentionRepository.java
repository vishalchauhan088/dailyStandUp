package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdateTicketMention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyUpdateTicketMentionRepository extends JpaRepository<DailyUpdateTicketMention, Long> {
    List<DailyUpdateTicketMention> findByDailyUpdateId(Long dailyUpdateId);
    List<DailyUpdateTicketMention> findByTicketId(Long ticketId);
}

