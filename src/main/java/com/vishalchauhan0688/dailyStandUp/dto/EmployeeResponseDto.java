package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class EmployeeResponseDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private List<TeamRoleInfo> teamRoles;
    private Instant updatedAt;
    private Instant createdAt;
    
    @Data
    public static class TeamRoleInfo {
        private Long teamId;
        private String teamName;
        private Long roleId;
        private String roleName;
    }
}
