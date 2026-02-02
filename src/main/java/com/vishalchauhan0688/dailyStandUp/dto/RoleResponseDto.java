package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for Role entity.
 * Includes role type information (SYSTEM vs TEAM).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {
    private Long id;
    private String roleName;
    private String roleType; // "SYSTEM" or "TEAM"
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Create DTO from entity
     */
    public static RoleResponseDto fromEntity(TeamRole teamRole) {
        return RoleResponseDto.builder()
                .id(teamRole.getId())
                .roleName(teamRole.getName())
                .createdAt(teamRole.getCreatedAt())
                .updatedAt(teamRole.getUpdatedAt())
                .build();
    }
}
