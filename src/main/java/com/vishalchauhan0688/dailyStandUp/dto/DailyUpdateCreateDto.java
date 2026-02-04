package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyUpdateCreateDto {
    @NotNull(message = "Team ID is required")
    private Long teamId;
    
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    private String generalNotes;
    
    private List<TicketMentionCreateDto> ticketMentions;
}
