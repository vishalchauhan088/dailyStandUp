package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateDto {
    @NotNull(message = "firstName is required")
    private String firstName;
    private String lastName;
    @NotNull(message = "email is required")
    private String email;
    private String password;
}
