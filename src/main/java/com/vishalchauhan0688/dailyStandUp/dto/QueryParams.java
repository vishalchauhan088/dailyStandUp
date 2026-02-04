package com.vishalchauhan0688.dailyStandUp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic query parameters for filtering, sorting, searching, and pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParams {
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    // Sorting: "field1:asc,field2:desc" or "field1,field2" (default asc)
    private String sort;
    
    // Search: full-text search across multiple fields
    private String search;
    
    // Filters: "field1:eq:value1,field2:gt:value2" or "field1:value1,field2:value2" (default eq)
    // Operators: eq, ne, gt, gte, lt, lte, like, in, between
    private String filter;
    
    // Field selection: "field1,field2" - only return these fields
    private String fields;
    
    public List<SortParam> getSortParams() {
        List<SortParam> sortParams = new ArrayList<>();
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                
                String[] sortParts = part.split(":");
                if (sortParts.length == 1) {
                    sortParams.add(new SortParam(sortParts[0], "asc"));
                } else if (sortParts.length == 2) {
                    sortParams.add(new SortParam(sortParts[0], sortParts[1].toLowerCase()));
                }
            }
        }
        return sortParams;
    }
    
    public List<FilterParam> getFilterParams() {
        List<FilterParam> filterParams = new ArrayList<>();
        if (filter != null && !filter.trim().isEmpty()) {
            String[] parts = filter.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;
                
                String[] filterParts = part.split(":");
                if (filterParts.length == 2) {
                    // field:value (default eq)
                    filterParams.add(new FilterParam(filterParts[0], "eq", filterParts[1]));
                } else if (filterParts.length == 3) {
                    // field:operator:value
                    filterParams.add(new FilterParam(filterParts[0], filterParts[1].toLowerCase(), filterParts[2]));
                } else if (filterParts.length == 4 && "between".equals(filterParts[1].toLowerCase())) {
                    // field:between:value1:value2
                    filterParams.add(new FilterParam(filterParts[0], "between", filterParts[2] + ":" + filterParts[3]));
                }
            }
        }
        return filterParams;
    }
    
    public List<String> getFieldSelection() {
        List<String> fieldsList = new ArrayList<>();
        if (fields != null && !fields.trim().isEmpty()) {
            String[] parts = fields.split(",");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty()) {
                    fieldsList.add(part);
                }
            }
        }
        return fieldsList;
    }
    
    @Data
    @AllArgsConstructor
    public static class SortParam {
        private String field;
        private String direction; // asc or desc
    }
    
    @Data
    @AllArgsConstructor
    public static class FilterParam {
        private String field;
        private String operator; // eq, ne, gt, gte, lt, lte, like, in, between
        private String value;
    }
}
