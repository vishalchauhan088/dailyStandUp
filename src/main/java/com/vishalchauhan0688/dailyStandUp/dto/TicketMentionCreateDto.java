package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketMentionCreateDto {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;
    
    @NotBlank(message = "Description is required")
    @NotNull(message = "Description is required")
    private String description;
}
