package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.Enum.TicketStatus;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {
    @Email
    @NotBlank
    @NotNull
    private String email;
    @NotBlank
    @NotNull
    private String password;

}
