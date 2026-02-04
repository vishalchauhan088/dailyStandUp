package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.model.DailyUpdatePost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for DailyUpdatePost entity.
 * Contains only the necessary fields for API responses, avoiding lazy-loading
 * issues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyUpdateResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeUsername;
    private Long teamId;
    private String teamName;
    private LocalDate date;
    private String generalNotes;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * List of ticket mentions.
     * Only included when explicitly requested.
     */
    private List<TicketMentionInfo> ticketMentions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketMentionInfo {
        private Long ticketId;
        private String jiraId;
        private String ticketTitle;
        private String description;
    }

    /**
     * Create DTO from entity with basic info (no ticket mentions)
     */
    public static DailyUpdateResponseDto fromEntity(DailyUpdatePost post) {
        return DailyUpdateResponseDto.builder()
                .id(post.getId())
                .employeeId(post.getEmployee() != null ? post.getEmployee().getId() : null)
                .employeeName(post.getEmployee() != null ? post.getEmployee().getName() : null)
                .employeeUsername(post.getEmployee() != null ? post.getEmployee().getUsername() : null)
                .teamId(post.getTeam() != null ? post.getTeam().getId() : null)
                .teamName(post.getTeam() != null ? post.getTeam().getTeamName() : null)
                .date(post.getDate())
                .generalNotes(post.getGeneralNotes())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
