package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.exception.ResourceNotFoundException;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import com.vishalchauhan0688.dailyStandUp.repository.TicketDependencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketDependencyService {
    private final TicketDependencyRepository dependencyRepository;
    private final TicketService ticketService;

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
        if (ticketId.equals(dependsOnTicketId)) {
            throw new BadRequestException("A ticket cannot depend on itself");
        }

        Ticket ticket = ticketService.findById(ticketId);
        Ticket dependsOnTicket = ticketService.findById(dependsOnTicketId);

        // Check if dependency already exists
        if (dependencyRepository.findByTicketIdAndDependsOnTicketId(ticketId, dependsOnTicketId).isPresent()) {
            throw new BadRequestException("Dependency already exists");
        }

        // Check for circular dependencies (basic check)
        if (dependencyRepository.findByTicketIdAndDependsOnTicketId(dependsOnTicketId, ticketId).isPresent()) {
            throw new BadRequestException("Circular dependency detected");
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
        TicketDependency dependency = findById(id);
        dependency.setResolved(resolved);
        return dependencyRepository.save(dependency);
    }

    @Transactional
    public void delete(Long id) {
        TicketDependency dependency = findById(id);
        dependencyRepository.delete(dependency);
    }
}
