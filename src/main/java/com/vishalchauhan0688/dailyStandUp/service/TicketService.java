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
    private final AuthorizationService authorizationService;
    private final StatusTransitionService statusTransitionService;
    private final QueryService queryService;

    /* ===================== FINDERS ===================== */

    public Ticket findById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        if (ticket.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Ticket has been deleted");
        }
        return ticket;
    }

    public List<Ticket> findAll() {
        return ticketRepository.findByDeletedAtIsNull();
    }

    public PageResponse<Ticket> findAll(QueryParams params) {
        Page<Ticket> page = queryService.query(ticketRepository, params, this::ticketSearchSpec);

        return PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    // Convert search string to Specification<Ticket>
    private Specification<Ticket> ticketSearchSpec(String search) {
        if (search == null || search.trim().isEmpty()) return null;

        String term = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> cb.and(
                cb.isNull(root.get("deletedAt")),
                cb.or(
                        cb.like(cb.lower(root.get("jiraId")), term),
                        cb.like(cb.lower(root.get("title")), term),
                        cb.like(cb.lower(root.get("description")), term)
                )
        );
    }

    /* ===================== CREATE ===================== */

    @Transactional
    public Ticket save(TicketCreateDto dto) {
        Long employeeId = employeeService.getMe().getId();
        
        // Authorization: User must be team member AND project member
        authorizationService.verifyCanCreateTicket(employeeId, dto.getProjectId());

        ensureJiraIdUnique(dto.getJiraId());

        Ticket ticket = Ticket.builder()
                .jiraId(dto.getJiraId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        // Status - required
        Status status = statusService.findById(dto.getStatusId());
        ticket.setStatus(status);

        // Owner/Assignee - defaults to current user
        Employee owner = dto.getOwnerId() != null
                ? employeeService.findByIdEntity(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getOwnerId()))
                : employeeService.getMe();
        
        // Verify owner is project member if specified
        if (dto.getOwnerId() != null && !authorizationService.isProjectMember(dto.getOwnerId(), dto.getProjectId())) {
            throw new BadRequestException("Ticket owner must be assigned to the project");
        }
        
        ticket.setOwner(owner);

        // Project - required
        Project project = projectService.findById(dto.getProjectId());
        ticket.setProject(project);

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
        Long employeeId = employeeService.getMe().getId();
        Ticket ticket = findById(id);
        
        // Authorization: Only ticket owner, OWNER, or MANAGER can update
        authorizationService.verifyCanUpdateTicket(employeeId, ticket);

        // Jira ID
        if (dto.getJiraId() != null && !ticket.getJiraId().equals(dto.getJiraId())) {
            ensureJiraIdUnique(dto.getJiraId());
            ticket.setJiraId(dto.getJiraId());
        }

        if (dto.getTitle() != null) ticket.setTitle(dto.getTitle());
        if (dto.getDescription() != null) ticket.setDescription(dto.getDescription());

        // Status update requires special authorization and validation
        if (dto.getStatusId() != null) {
            authorizationService.verifyCanUpdateTicketStatus(employeeId, ticket);
            Status newStatus = statusService.findById(dto.getStatusId());
            
            // Validate status transition
            statusTransitionService.validateTicketStatusTransition(ticket, newStatus);
            
            ticket.setStatus(newStatus);
        }

        if (dto.getOwnerId() != null) {
            // Only OWNER or MANAGER can reassign tickets
            Project project = ticket.getProject();
            if (!authorizationService.hasRoleInTeam(employeeId, project.getTeam().getId(), "OWNER", "MANAGER")) {
                throw new BadRequestException("Only OWNER or MANAGER can reassign tickets");
            }
            
            Employee owner = employeeService.findByIdEntity(dto.getOwnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getOwnerId()));
            
            if (!authorizationService.isProjectMember(dto.getOwnerId(), ticket.getProject().getId())) {
                throw new BadRequestException("New owner must be assigned to the project");
            }
            
            ticket.setOwner(owner);
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
        Long employeeId = employeeService.getMe().getId();
        Ticket ticket = findById(id);
        
        // Authorization: Only OWNER or MANAGER can delete tickets
        authorizationService.verifyCanDeleteTicket(employeeId, ticket);

        if (!ticket.getChildTickets().isEmpty()) {
            throw new BadRequestException("Cannot delete ticket with child tickets");
        }

        // Soft delete
        ticket.setDeletedAt(java.time.Instant.now());
        ticketRepository.save(ticket);
    }

    /* ===================== HELPERS ===================== */

    private void ensureJiraIdUnique(String jiraId) {
        ticketRepository.findByJiraId(jiraId)
                .ifPresent(t -> {
                    if (t.getDeletedAt() == null) {
                        throw new BadRequestException("Ticket with Jira ID already exists: " + jiraId);
                    }
                });
    }
}
