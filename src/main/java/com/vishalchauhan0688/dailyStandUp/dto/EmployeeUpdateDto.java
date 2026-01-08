package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeUpdateDto {
    //make all field optional
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
