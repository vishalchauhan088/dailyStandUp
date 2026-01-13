package com.vishalchauhan0688.dailyStandUp.query.schema;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Getter
@Setter
@RequiredArgsConstructor
public class FieldSchema {
    private final String name;
    private final Class<?> type;
    private final Set<Operator> allowedOperators;
    private final boolean sortable;
}
