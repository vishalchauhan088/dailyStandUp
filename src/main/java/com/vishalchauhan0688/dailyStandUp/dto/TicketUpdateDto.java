package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketUpdateDto {
    @Size(min = 1, max = 50, message = "Jira ID must be between 1 and 50 characters")
    private String jiraId;

    @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Long statusId;

    private Long parentTicketId;

    private Long ownerId;

    private LocalDate startDate;

    private LocalDate endDate;
}
