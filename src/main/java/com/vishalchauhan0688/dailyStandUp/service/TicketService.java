package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.dto.PageResponse;
import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import com.vishalchauhan0688.dailyStandUp.dto.TicketCreateDto;
import com.vishalchauhan0688.dailyStandUp.dto.TicketUpdateDto;
import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.model.Project;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.repository.TicketRepository;
import com.vishalchauhan0688.dailyStandUp.util.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeService employeeService;
    private final StatusService statusService;
    private final ProjectService projectService;
    private final QueryService queryService;

    /* ===================== FINDERS ===================== */

    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public PageResponse<Ticket> findAll(QueryParams params) {
        // Pass method reference instead of Specification instance
        Page<Ticket> page = queryService.query(ticketRepository, params, this::ticketSearchSpec);

        return PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    // Convert search string to Specification<Ticket>
    private Specification<Ticket> ticketSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) return null;

        String term = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("externalId")), term),
                cb.like(cb.lower(root.get("title")), term),
                cb.like(cb.lower(root.get("description")), term)
        );
    }

    /* ===================== CREATE ===================== */

    @Transactional
    public Ticket save(TicketCreateDto dto) {
        ensureExternalIdUnique(dto.getExternalId());

        Ticket ticket = new Ticket();
        ticket.setExternalId(dto.getExternalId());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStartDate(dto.getStartDate());
        ticket.setEndDate(dto.getEndDate());

        // Status
        Status status = statusService.findById(dto.getStatusId());
        ticket.setStatus(status);

        // Owner/Assignee
        Employee assignee = dto.getEmployeeId() != null
                ? employeeService.findByIdEntity(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()))
                : employeeService.getMe();
        ticket.setCreatedBy(assignee);

        // Project
        if (dto.getProjectId() != null) {
            Project project = projectService.findById(dto.getProjectId());
            ticket.setProject(project);
        }

        // Parent ticket
        if (dto.getParentTicketId() != null) {
            Ticket parent = findById(dto.getParentTicketId());
            ticket.setParentTicket(parent);
        }

        return ticketRepository.save(ticket);
    }

    /* ===================== UPDATE ===================== */

    @Transactional
    public Ticket update(Long id, TicketUpdateDto dto) {
        Ticket ticket = findById(id);

        // External ID
        if (dto.getExternalId() != null && !ticket.getExternalId().equals(dto.getExternalId())) {
            ensureExternalIdUnique(dto.getExternalId());
            ticket.setExternalId(dto.getExternalId());
        }

        if (dto.getTitle() != null) ticket.setTitle(dto.getTitle());
        if (dto.getDescription() != null) ticket.setDescription(dto.getDescription());

        if (dto.getStatusId() != null) {
            Status status = statusService.findById(dto.getStatusId());
            ticket.setStatus(status);
        }

        if (dto.getEmployeeId() != null) {
            Employee assignee = employeeService.findByIdEntity(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));
            ticket.setCreatedBy(assignee);
        }

        if (dto.getProjectId() != null) {
            Project project = projectService.findById(dto.getProjectId());
            ticket.setProject(project);
        }

        if (dto.getParentTicketId() != null) {
            if (dto.getParentTicketId().equals(id)) {
                throw new BadRequestException("A ticket cannot be its own parent");
            }
            Ticket parent = findById(dto.getParentTicketId());
            ticket.setParentTicket(parent);
        }

        if (dto.getStartDate() != null) ticket.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) ticket.setEndDate(dto.getEndDate());

        return ticketRepository.save(ticket);
    }

    /* ===================== DELETE ===================== */

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findById(id);

        if (!ticket.getChildTickets().isEmpty()) {
            throw new BadRequestException("Cannot delete ticket with child tickets");
        }

        ticketRepository.delete(ticket);
    }

    /* ===================== HELPERS ===================== */

    private void ensureExternalIdUnique(String externalId) {
        ticketRepository.findByExternalId(externalId)
                .ifPresent(t -> {
                    throw new BadRequestException("Ticket with Jira ID already exists: " + externalId);
                });
    }
}
