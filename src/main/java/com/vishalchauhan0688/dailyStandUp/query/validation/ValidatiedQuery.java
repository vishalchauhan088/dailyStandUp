package com.vishalchauhan0688.dailyStandUp.query.validation;

import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ValidatiedQuery {
    private final List<FilterCondition> filters;
    private final List<SortCondition> sorts;
    private final int page;
    private final int size;
}
