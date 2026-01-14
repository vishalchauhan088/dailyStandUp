package com.vishalchauhan0688.dailyStandUp.query.parser;

import com.vishalchauhan0688.dailyStandUp.query.Direction;
import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import com.vishalchauhan0688.dailyStandUp.query.request.QueryRequest;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import com.vishalchauhan0688.dailyStandUp.query.schema.Operator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Component
public class QueryParser {

    public static QueryRequest parse(Map<String, String[]> queryParams) {
        QueryRequest qr = new QueryRequest();
        qr.setFilters(parseFilters(queryParams));
        qr.setSorts(parseSorts(queryParams));
        qr.setPage(parseSingleInt(queryParams, "page"));
        qr.setSize(parseSingleInt(queryParams, "size"));
        return qr;
    }

    private static Integer parseSingleInt(Map<String, String[]> params, String key) {
        if (!params.containsKey(key)) return null;
        String[] values = params.get(key);
        if (values == null || values.length == 0) return null;
        return Integer.parseInt(values[0]);
    }

    private static List<FilterCondition> parseFilters(Map<String, String[]> params) {
        List<FilterCondition> filters = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String rawKey = entry.getKey();
            String[] rawValue = entry.getValue();

            if (isReservedParam(rawKey)) continue;

            ParsedKey parsed = parseOperator(rawKey);

            FilterCondition fc = new FilterCondition();
            fc.setField(parsed.field);
            fc.setOperator(parsed.operator);
            fc.setValues(parseValues(rawValue));

            filters.add(fc);
        }
        return filters;
    }

    private static ParsedKey parseOperator(String key) {
        if (key.endsWith(">=")) return new ParsedKey(key.replace(">=", ""), Operator.GTE);
        if (key.endsWith("<=")) return new ParsedKey(key.replace("<=", ""), Operator.LTE);
        if (key.endsWith(">"))  return new ParsedKey(key.replace(">", ""), Operator.GT);
        if (key.endsWith("<"))  return new ParsedKey(key.replace("<", ""), Operator.LT);
        return new ParsedKey(key, Operator.EQ);
    }

    private static List<String> parseValues(String[] values) {
        List<String> result = new ArrayList<>();
        for (String v : values) {
            result.addAll(Arrays.asList(v.split(",")));
        }
        return result;
    }

    private static List<SortCondition> parseSorts(Map<String, String[]> params) {
        String[] sorts = params.get("sort");
        if (sorts == null) return null;

        List<SortCondition> result = new ArrayList<>();
        for (String sort : sorts) {
            String[] parts = sort.split(",");
            SortCondition sc = new SortCondition();
            sc.setField(parts[0]);
            sc.setDirection(
                    parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? Direction.DESC
                            : Direction.ASC
            );
            result.add(sc);
        }
        return result;
    }

    private static boolean isReservedParam(String key) {
        return key.equals("page") || key.equals("size") || key.equals("sort");
    }
}
