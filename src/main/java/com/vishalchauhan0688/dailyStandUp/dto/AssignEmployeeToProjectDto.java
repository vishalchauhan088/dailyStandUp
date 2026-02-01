package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignEmployeeToProjectDto {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}

