package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCreateDto {

    @NotNull(message = "username required")
    @Min(value = 2)
    private String userName;
    @NotNull(message = "firstName is required")
    private String firstName;
    private String lastName;
    @NotNull(message = "email is required")
    private String email;
    @NotNull
    @Min(value=4)
    private String password;
}
