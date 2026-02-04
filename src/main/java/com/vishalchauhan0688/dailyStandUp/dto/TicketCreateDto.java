package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketCreateDto {
    @NotBlank(message = "Jira ID is required")
    private String jiraId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status ID is required")
    private Long statusId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long parentTicketId;

    private Long ownerId; // Owner/assignee - defaults to current user if not provided

    private LocalDate startDate;

    private LocalDate endDate;
}
