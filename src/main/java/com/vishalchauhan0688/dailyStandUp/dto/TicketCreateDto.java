package com.vishalchauhan0688.dailyStandUp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TicketCreateDto {

    @NotNull
    String externalId;
    String title;

}
