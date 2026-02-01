package com.vishalchauhan0688.dailyStandUp.repository;

import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByExternalId(String externalId);
    List<Ticket> findByStatus(Status status);
    List<Ticket> findByStatusId(Long statusId);
    List<Ticket> findByProject(Project project);
    List<Ticket> findByProjectId(Long projectId);
    List<Ticket> findByCreatedById(Long employeeId);
    List<Ticket> findByParentTicketId(Long parentTicketId);
    
    @Query("SELECT t FROM Ticket t WHERE t.externalId LIKE %:query% OR t.title LIKE %:query%")
    List<Ticket> search(@Param("query") String query);
}
