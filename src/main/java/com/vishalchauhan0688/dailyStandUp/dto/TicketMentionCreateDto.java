package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketMentionCreateDto {
    @NotNull
    @NotBlank
    Long ticketId;
    @NotBlank
    @NotNull
    String description;
}
