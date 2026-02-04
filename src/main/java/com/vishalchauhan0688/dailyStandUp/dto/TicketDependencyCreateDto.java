package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDependencyCreateDto {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Depends on Ticket ID is required")
    private Long dependsOnTicketId;
}

