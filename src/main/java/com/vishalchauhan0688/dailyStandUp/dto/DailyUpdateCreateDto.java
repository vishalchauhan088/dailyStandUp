package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.Data;

import java.util.List;

@Data
public class DailyUpdateCreateDto {

    String generalDesciption;
    List<TicketMentionCreateDto> ticketMentions;
}
