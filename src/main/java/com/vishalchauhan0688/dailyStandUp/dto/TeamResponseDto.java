package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for Team entity.
 * Contains only the necessary fields for API responses, avoiding lazy-loading
 * issues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDto {
    private Long id;
    private String teamName;
    private String description;
    private Integer memberCount;
    private Integer projectCount;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * List of team members with their roles.
     * Only included when explicitly requested to avoid N+1 queries.
     */
    private List<TeamMemberInfo> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberInfo {
        private Long employeeId;
        private String username;
        private String name;
        private String email;
        private String roleName;
    }
}
