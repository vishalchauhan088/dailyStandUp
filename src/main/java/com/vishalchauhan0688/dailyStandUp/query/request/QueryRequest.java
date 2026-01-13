package com.vishalchauhan0688.dailyStandUp.query.request;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class QueryRequest {
    private List<FilterCondition> filters;
    private List<SortCondition> sorts;
    private Integer page;
    private Integer size;
}
