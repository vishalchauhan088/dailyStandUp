package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for Project entity.
 * Contains only the necessary fields for API responses, avoiding lazy-loading
 * issues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String projectName;
    private String projectDescription;
    private Long teamId;
    private String teamName;
    private Integer ticketCount;
    private Integer memberCount;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * List of project members.
     * Only included when explicitly requested to avoid N+1 queries.
     */
    private List<ProjectMemberInfo> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectMemberInfo {
        private Long employeeId;
        private String username;
        private String name;
        private String email;
    }
}
