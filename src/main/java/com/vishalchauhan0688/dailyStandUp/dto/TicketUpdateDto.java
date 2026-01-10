package com.vishalchauhan0688.dailyStandUp.dto;

import com.vishalchauhan0688.dailyStandUp.Enum.TicketStatus;
import com.vishalchauhan0688.dailyStandUp.model.Employee;
import lombok.Data;


@Data
public  class TicketUpdateDto {
    String externalId;
    String title;
    TicketStatus status;
    Long employeeId;

}