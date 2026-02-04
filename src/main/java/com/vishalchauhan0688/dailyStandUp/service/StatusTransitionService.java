package com.vishalchauhan0688.dailyStandUp.service;

import com.vishalchauhan0688.dailyStandUp.exception.BadRequestException;
import com.vishalchauhan0688.dailyStandUp.model.Status;
import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import com.vishalchauhan0688.dailyStandUp.model.TicketDependency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service to validate and manage ticket status transitions
 * Enforces valid status flow: TO_DO → IN_PROGRESS → BLOCKED → REVIEW → DONE
 */
@Service
@RequiredArgsConstructor
public class StatusTransitionService {
    private final TicketDependencyService ticketDependencyService;

    // Valid status transitions
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "TO_DO", Set.of("IN_PROGRESS", "BLOCKED"),
            "IN_PROGRESS", Set.of("BLOCKED", "REVIEW", "TO_DO"),
            "BLOCKED", Set.of("TO_DO", "IN_PROGRESS"),
            "REVIEW", Set.of("IN_PROGRESS", "DONE", "BLOCKED"),
            "DONE", Set.of() // DONE is terminal
    );

    /**
     * Validate if status transition is allowed
     */
    public void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new BadRequestException("Status cannot be null");
        }

        if (currentStatus.equalsIgnoreCase(newStatus)) {
            return; // Same status is allowed
        }

        Set<String> allowedTransitions = VALID_TRANSITIONS.get(currentStatus.toUpperCase());
        if (allowedTransitions == null) {
            throw new BadRequestException("Unknown current status: " + currentStatus);
        }

        if (!allowedTransitions.contains(newStatus.toUpperCase())) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s. Allowed transitions: %s",
                            currentStatus, newStatus, allowedTransitions)
            );
        }
    }

    /**
     * Validate ticket can transition to new status
     * Additional business rules:
     * - Blocked tickets cannot move to DONE
     * - Tickets with unresolved dependencies cannot move to DONE
     */
    public void validateTicketStatusTransition(Ticket ticket, Status newStatus) {
        String currentStatus = ticket.getStatus().getStatus();
        String newStatusName = newStatus.getStatus();

        // Basic transition validation
        validateStatusTransition(currentStatus, newStatusName);

        // Blocked tickets cannot move to DONE
        if ("BLOCKED".equalsIgnoreCase(currentStatus) && "DONE".equalsIgnoreCase(newStatusName)) {
            throw new BadRequestException("Blocked tickets cannot be moved directly to DONE");
        }

        // Check for unresolved dependencies if moving to DONE
        if ("DONE".equalsIgnoreCase(newStatusName)) {
            List<TicketDependency> unresolvedDeps = ticketDependencyService.findUnresolvedDependenciesByTicketId(ticket.getId());
            if (!unresolvedDeps.isEmpty()) {
                throw new BadRequestException(
                        "Cannot move ticket to DONE. There are " + unresolvedDeps.size() + " unresolved dependencies");
            }
        }
    }
}

