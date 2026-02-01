package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMemberRoleDto {
    @NotNull(message = "Role ID is required")
    private Long roleId;
}

