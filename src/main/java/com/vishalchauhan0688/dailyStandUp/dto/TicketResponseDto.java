package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for Ticket entity.
 * Contains only the necessary fields for API responses, avoiding lazy-loading
 * issues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {
    private Long id;
    private String jiraId;
    private String title;
    private String description;

    // Owner info
    private Long ownerId;
    private String ownerName;
    private String ownerUsername;

    // Status info
    private Long statusId;
    private String statusName;

    // Project info
    private Long projectId;
    private String projectName;
    private Long teamId;
    private String teamName;

    // Parent ticket info (optional)
    private Long parentTicketId;
    private String parentTicketJiraId;

    // Dates
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * List of child tickets.
     * Only included when explicitly requested.
     */
    private List<ChildTicketInfo> childTickets;

    /**
     * List of dependencies.
     * Only included when explicitly requested.
     */
    private List<DependencyInfo> dependencies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildTicketInfo {
        private Long id;
        private String jiraId;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DependencyInfo {
        private Long id;
        private Long dependsOnTicketId;
        private String dependsOnJiraId;
        private String dependsOnTitle;
        private Boolean resolved;
    }

    /**
     * Create DTO from entity with basic info (no child tickets or dependencies)
     */
    public static TicketResponseDto fromEntity(Ticket ticket) {
        return TicketResponseDto.builder()
                .id(ticket.getId())
                .jiraId(ticket.getJiraId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .ownerId(ticket.getOwner() != null ? ticket.getOwner().getId() : null)
                .ownerName(ticket.getOwner() != null ? ticket.getOwner().getName() : null)
                .ownerUsername(ticket.getOwner() != null ? ticket.getOwner().getUsername() : null)
                .statusId(ticket.getStatus() != null ? ticket.getStatus().getId() : null)
                .statusName(ticket.getStatus() != null ? ticket.getStatus().getStatus() : null)
                .projectId(ticket.getProject() != null ? ticket.getProject().getId() : null)
                .projectName(ticket.getProject() != null ? ticket.getProject().getProjectName() : null)
                .teamId(ticket.getProject() != null && ticket.getProject().getTeam() != null
                        ? ticket.getProject().getTeam().getId()
                        : null)
                .teamName(ticket.getProject() != null && ticket.getProject().getTeam() != null
                        ? ticket.getProject().getTeam().getTeamName()
                        : null)
                .parentTicketId(ticket.getParentTicket() != null ? ticket.getParentTicket().getId() : null)
                .parentTicketJiraId(ticket.getParentTicket() != null ? ticket.getParentTicket().getJiraId() : null)
                .startDate(ticket.getStartDate())
                .endDate(ticket.getEndDate())
                .deletedAt(ticket.getDeletedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
