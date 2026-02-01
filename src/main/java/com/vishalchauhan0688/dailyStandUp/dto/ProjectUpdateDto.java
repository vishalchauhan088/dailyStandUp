package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectUpdateDto {
    @Size(min = 2, max = 255, message = "Project name must be between 2 and 255 characters")
    private String projectName;

    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    private String projectDescription;
}

