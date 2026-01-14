package com.vishalchauhan0688.dailyStandUp.query.normalization;

import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NormalizedQuery  {
   List<FilterCondition> filters;
   List<SortCondition> sorts;
    private Integer page;
    private Integer size;
}
