package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketUpdateDto {
    private String jiraId;
    private String title;
    private String description;
    private Long statusId;
    private Long projectId;
    private Long parentTicketId;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
}