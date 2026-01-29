package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDataDto {
    private String token;
    private EmployeeResponseDto user;
}
