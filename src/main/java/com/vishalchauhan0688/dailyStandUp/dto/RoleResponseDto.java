package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.model.Role;
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
    public static RoleResponseDto fromEntity(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleType(role.getRoleType() != null ? role.getRoleType().name() : "TEAM")
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
