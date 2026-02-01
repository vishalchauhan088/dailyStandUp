package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import com.vishalchauhan0688.dailyStandUp.repository.TicketDependencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TicketDependencyService {
    private final TicketDependencyRepository dependencyRepository;
    private final TicketService ticketService;
    private final AuthorizationService authorizationService;
    private final EmployeeService employeeService;

    public List<TicketDependency> findAll() {
        return dependencyRepository.findAll();
    }

    public TicketDependency findById(Long id) {
        return dependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket dependency not found with id: " + id));
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

    @Transactional
    public TicketDependency create(Long ticketId, Long dependsOnTicketId) {
        Long employeeId = employeeService.getMe().getId();
        
        if (ticketId.equals(dependsOnTicketId)) {
            throw new BadRequestException("A ticket cannot depend on itself");
        }

        Ticket ticket = ticketService.findById(ticketId);
        Ticket dependsOnTicket = ticketService.findById(dependsOnTicketId);

        // Verify tickets belong to same project
        if (!ticket.getProject().getId().equals(dependsOnTicket.getProject().getId())) {
            throw new BadRequestException("Tickets must belong to the same project to have dependencies");
        }

        // Authorization: User must be able to update the ticket
        authorizationService.verifyCanUpdateTicket(employeeId, ticket);

        // Check if dependency already exists
        if (dependencyRepository.findByTicketIdAndDependsOnTicketId(ticketId, dependsOnTicketId).isPresent()) {
            throw new BadRequestException("Dependency already exists");
        }

        // Check for circular dependencies (deep check)
        if (wouldCreateCircularDependency(ticketId, dependsOnTicketId)) {
            throw new BadRequestException("Circular dependency detected: This dependency would create a cycle");
        }

        TicketDependency dependency = TicketDependency.builder()
                .ticket(ticket)
                .dependsOnTicket(dependsOnTicket)
                .resolved(false)
                .build();

        return dependencyRepository.save(dependency);
    }

    /**
     * Check if adding this dependency would create a circular dependency
     * Uses DFS to detect cycles in the dependency graph
     */
    private boolean wouldCreateCircularDependency(Long ticketId, Long dependsOnTicketId) {
        // If dependsOnTicket already depends on ticketId (directly or indirectly), it's a cycle
        Set<Long> visited = new HashSet<>();
        return hasPathTo(dependsOnTicketId, ticketId, visited);
    }

    /**
     * Check if there's a path from startTicketId to targetTicketId in the dependency graph
     */
    private boolean hasPathTo(Long startTicketId, Long targetTicketId, Set<Long> visited) {
        if (startTicketId.equals(targetTicketId)) {
            return true; // Found path - cycle detected
        }

        if (visited.contains(startTicketId)) {
            return false; // Already visited this path
        }

        visited.add(startTicketId);

        // Get all tickets that startTicketId depends on
        List<TicketDependency> dependencies = dependencyRepository.findByTicketId(startTicketId);
        for (TicketDependency dep : dependencies) {
            if (hasPathTo(dep.getDependsOnTicket().getId(), targetTicketId, visited)) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public TicketDependency markResolved(Long id, Boolean resolved) {
        Long employeeId = employeeService.getMe().getId();
        TicketDependency dependency = findById(id);
        
        // Authorization: User must be able to update the ticket
        authorizationService.verifyCanUpdateTicket(employeeId, dependency.getTicket());
        
        dependency.setResolved(resolved);
        return dependencyRepository.save(dependency);
    }

    @Transactional
    public void delete(Long id) {
        Long employeeId = employeeService.getMe().getId();
        TicketDependency dependency = findById(id);
        
        // Authorization: User must be able to update the ticket
        authorizationService.verifyCanUpdateTicket(employeeId, dependency.getTicket());
        
        dependencyRepository.delete(dependency);
    }
}
