package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> {
    private Integer statusCode;
    private String message;
    private T data;
//    private Long timestamp;
}
