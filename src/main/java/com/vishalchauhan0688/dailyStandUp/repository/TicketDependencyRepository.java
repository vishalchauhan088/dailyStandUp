package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketDependencyRepository extends JpaRepository<TicketDependency, Long> {
    List<TicketDependency> findByTicket(Ticket ticket);
    List<TicketDependency> findByDependsOnTicket(Ticket dependsOnTicket);
    List<TicketDependency> findByTicketId(Long ticketId);
    List<TicketDependency> findByDependsOnTicketId(Long dependsOnTicketId);
    List<TicketDependency> findByResolved(Boolean resolved);
    
    @Query("SELECT td FROM TicketDependency td WHERE td.ticket.id = :ticketId AND td.resolved = false")
    List<TicketDependency> findUnresolvedDependenciesByTicketId(@Param("ticketId") Long ticketId);
    
    @Query("SELECT td FROM TicketDependency td WHERE td.dependsOnTicket.id = :ticketId AND td.resolved = false")
    List<TicketDependency> findBlockingTickets(@Param("ticketId") Long ticketId);
    
    Optional<TicketDependency> findByTicketIdAndDependsOnTicketId(Long ticketId, Long dependsOnTicketId);
}
