package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberToTeamDto {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}

