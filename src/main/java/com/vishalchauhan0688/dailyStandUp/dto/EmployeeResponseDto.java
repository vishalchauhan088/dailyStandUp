package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.model.Role;
import com.vishalchauhan0688.dailyStandUp.model.Team;
import lombok.Data;

import java.time.Instant;

@Data
public class EmployeeResponseDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String managerName;
    private Long managerId;
    private Long teamId;
    private String teamName;
    private Role role;
    private Instant updated_at;
    private Instant created_at;
}
