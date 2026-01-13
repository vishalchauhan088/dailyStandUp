package com.vishalchauhan0688.dailyStandUp.query.request;

import com.vishalchauhan0688.dailyStandUp.query.schema.Operator;
import lombok.Data;

import java.util.List;

@Data
public class FilterCondition {
    private String field;
    private Operator operator;
    private List<String> values;
}
