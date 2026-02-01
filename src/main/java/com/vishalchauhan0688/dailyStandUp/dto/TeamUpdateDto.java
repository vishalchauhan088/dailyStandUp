package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamUpdateDto {
    @Size(min = 2, max = 255, message = "Team name must be between 2 and 255 characters")
    private String teamName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

