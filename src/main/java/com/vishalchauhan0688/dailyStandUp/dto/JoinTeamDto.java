package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinTeamDto {
    @NotNull(message = "Team ID is required")
    private Long teamId;
}
