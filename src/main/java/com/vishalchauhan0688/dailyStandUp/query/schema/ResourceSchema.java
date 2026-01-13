package com.vishalchauhan0688.dailyStandUp.query.schema;

import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class ResourceSchema {
    private final String resourceName;
    private final Map<String, FieldSchema> fields;
    private final int defaultPageSize;
    private final int maxPageSize;
    private final List<SortCondition> defaultSort;

}
