package com.vishalchauhan0688.dailyStandUp.query.parser;

import com.vishalchauhan0688.dailyStandUp.query.schema.Operator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ParsedKey {
    final String field;
    final Operator operator;
}
