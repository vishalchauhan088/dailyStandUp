package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import com.vishalchauhan0688.dailyStandUp.repository.TicketDependencyRepository;
import com.vishalchauhan0688.dailyStandUp.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketDependencyService {

    private final TicketDependencyRepository dependencyRepository;
    private final TicketRepository ticketRepository;
    private final AuthorizationService authorizationService;
    private final EmployeeService employeeService;

    /* =========================
       Read Operations
       ========================= */

    public List<TicketDependency> findAll() {
        return dependencyRepository.findAll();
    }

    public TicketDependency findById(Long id) {
        return dependencyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket dependency not found with id: " + id)
                );
    }

    public List<TicketDependency> findByTicketId(Long ticketId) {
        return dependencyRepository.findByTicketId(ticketId);
    }

    public List<TicketDependency> findUnresolvedDependenciesByTicketId(Long ticketId) {
        return dependencyRepository.findUnresolvedDependenciesByTicketId(ticketId);
    }

    public List<TicketDependency> findBlockingTickets(Long ticketId) {
        return dependencyRepository.findBlockingTickets(ticketId);
    }

    /* =========================
       Write Operations
       ========================= */

    @Transactional
    public TicketDependency create(Long ticketId, Long dependsOnTicketId) {
        Long employeeId = employeeService.getMe().getId();

        if (ticketId.equals(dependsOnTicketId)) {
            throw new BadRequestException("A ticket cannot depend on itself");
        }

        Ticket ticket = getTicket(ticketId);
        Ticket dependsOnTicket = getTicket(dependsOnTicketId);

        // Tickets must belong to the same project
        if (!ticket.getProject().getId().equals(dependsOnTicket.getProject().getId())) {
            throw new BadRequestException(
                    "Tickets must belong to the same project to have dependencies"
            );
        }

        // Authorization
        authorizationService.verifyCanUpdateTicket(employeeId, ticket);

        // Duplicate dependency check
        if (dependencyRepository
                .findByTicketIdAndDependsOnTicketId(ticketId, dependsOnTicketId)
                .isPresent()) {
            throw new BadRequestException("Dependency already exists");
        }

        // Circular dependency check
        if (wouldCreateCircularDependency(ticketId, dependsOnTicketId)) {
            throw new BadRequestException(
                    "Circular dependency detected: This dependency would create a cycle"
            );
        }

        TicketDependency dependency = TicketDependency.builder()
                .ticket(ticket)
                .dependsOnTicket(dependsOnTicket)
                .resolved(false)
                .build();

        return dependencyRepository.save(dependency);
    }

    @Transactional
    public TicketDependency markResolved(Long id, Boolean resolved) {
        Long employeeId = employeeService.getMe().getId();
        TicketDependency dependency = findById(id);

        authorizationService.verifyCanUpdateTicket(employeeId, dependency.getTicket());

        dependency.setResolved(resolved);
        return dependencyRepository.save(dependency);
    }

    @Transactional
    public void delete(Long id) {
        Long employeeId = employeeService.getMe().getId();
        TicketDependency dependency = findById(id);

        authorizationService.verifyCanUpdateTicket(employeeId, dependency.getTicket());

        dependencyRepository.delete(dependency);
    }

    /* =========================
       Internal Helpers
       ========================= */

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found with id: " + ticketId)
                );
    }

    /**
     * Checks whether adding (ticketId -> dependsOnTicketId) would create a cycle
     */
    private boolean wouldCreateCircularDependency(Long ticketId, Long dependsOnTicketId) {
        Set<Long> visited = new HashSet<>();
        return hasPathTo(dependsOnTicketId, ticketId, visited);
    }

    /**
     * DFS traversal to check reachability in dependency graph
     */
    private boolean hasPathTo(Long currentId, Long targetId, Set<Long> visited) {
        if (currentId.equals(targetId)) {
            return true;
        }

        if (!visited.add(currentId)) {
            return false;
        }

        List<TicketDependency> dependencies =
                dependencyRepository.findByTicketId(currentId);

        for (TicketDependency dependency : dependencies) {
            if (hasPathTo(
                    dependency.getDependsOnTicket().getId(),
                    targetId,
                    visited)) {
                return true;
            }
        }

        return false;
    }
}
