package com.vishalchauhan0688.dailyStandUp.repository;

import ch.qos.logback.core.status.Status;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByExternalId(String ticketId);
    List<Ticket> findByStatus(Status status);
}
