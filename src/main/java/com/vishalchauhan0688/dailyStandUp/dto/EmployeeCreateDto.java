package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateDto {
    @NotBlank(message = "Username is required")
    @Min(value = 2, message = "Username must be at least 2 characters")
    private String userName;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Min(value = 4, message = "Password must be at least 4 characters")
    private String password;

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private Long managerId; // Optional
}
