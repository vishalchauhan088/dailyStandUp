package com.vishalchauhan0688.dailyStandUp.util;

import com.vishalchauhan0688.dailyStandUp.dto.QueryParams;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Generic query service for filtering, sorting, searching, and pagination
 * Works with any JPA entity that extends JpaSpecificationExecutor
 */
@Service
@RequiredArgsConstructor
public class QueryService {

    /**
     * Execute query with filtering, sorting, searching, and pagination
     */
    public <T> Page<T> query(
            JpaSpecificationExecutor<T> repository,
            QueryParams params,
            Function<String, Specification<T>> searchSpecification) {

        // Build specification from filters and search
        Specification<T> spec = buildSpecification(params, searchSpecification);

        // Build sort
        Sort sort = buildSort(params);

        // Build pageable
        Pageable pageable = PageRequest.of(params.getPage(), params.getSize(), sort);

        // Execute query
        return repository.findAll(spec, pageable);
    }

    /**
     * Build JPA Specification from query parameters
     */
    private <T> Specification<T> buildSpecification(
            QueryParams params,
            Function<String, Specification<T>> searchSpecification) {

        Specification<T> spec = (root, query, cb) -> cb.conjunction();

        // Add search specification
        if (params.getSearch() != null && !params.getSearch().trim().isEmpty()) {
            Specification<T> searchSpec = searchSpecification.apply(params.getSearch());
            if (searchSpec != null) {
                spec = spec.and(searchSpec);
            }
        }

        // Add filter specifications
        for (QueryParams.FilterParam filter : params.getFilterParams()) {
            spec = spec.and(createFilterSpecification(filter));
        }

        return spec;
    }

    /**
     * Create a filter specification for a single filter parameter
     */
    public <T> Specification<T> createFilterSpecification(QueryParams.FilterParam filter) {
        return (root, query, cb) -> createFilterPredicate(root, query, cb, filter);
    }

    /**
     * Create a filter predicate directly for use in existing specifications
     * This avoids the type mismatch between Specification and Predicate
     */
    public <T> Predicate createFilterPredicate(
            jakarta.persistence.criteria.Root<T> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            QueryParams.FilterParam filter) {
        try {
            Path<?> field = getFieldPath(root, filter.getField());
            Class<?> fieldType = field.getJavaType();
            Object value = parseValue(fieldType, filter.getValue());

            if (value == null && !filter.getOperator().equalsIgnoreCase("eq")) {
                return null; // Can't compare null with operators other than eq
            }

            switch (filter.getOperator().toLowerCase()) {
                case "eq":
                    if (value == null) {
                        return cb.isNull(field);
                    }
                    return cb.equal(field, value);
                case "ne":
                    if (value == null) {
                        return cb.isNotNull(field);
                    }
                    return cb.notEqual(field, value);
                case "gt":
                    if (!(value instanceof Comparable)) {
                        throw new IllegalArgumentException(
                                "Cannot use gt operator on non-comparable type: " + fieldType.getSimpleName());
                    }
                    return cb.greaterThan((Expression<Comparable>) field, (Comparable) value);
                case "gte":
                    if (!(value instanceof Comparable)) {
                        throw new IllegalArgumentException(
                                "Cannot use gte operator on non-comparable type: " + fieldType.getSimpleName());
                    }
                    return cb.greaterThanOrEqualTo((Expression<Comparable>) field, (Comparable) value);
                case "lt":
                    if (!(value instanceof Comparable)) {
                        throw new IllegalArgumentException(
                                "Cannot use lt operator on non-comparable type: " + fieldType.getSimpleName());
                    }
                    return cb.lessThan((Expression<Comparable>) field, (Comparable) value);
                case "lte":
                    if (!(value instanceof Comparable)) {
                        throw new IllegalArgumentException(
                                "Cannot use lte operator on non-comparable type: " + fieldType.getSimpleName());
                    }
                    return cb.lessThanOrEqualTo((Expression<Comparable>) field, (Comparable) value);
                case "like":
                    if (fieldType != String.class) {
                        throw new IllegalArgumentException(
                                "Like operator can only be used on String fields, got: " + fieldType.getSimpleName());
                    }
                    return cb.like(cb.lower((Expression<String>) field), "%" + value.toString().toLowerCase() + "%");
                case "in":
                    List<Object> inValues = parseListValue(fieldType, filter.getValue());
                    return field.in(inValues);
                case "between":
                    String[] values = filter.getValue().split(":");
                    if (values.length == 2) {
                        Object value1 = parseValue(fieldType, values[0]);
                        Object value2 = parseValue(fieldType, values[1]);
                        if (!(value1 instanceof Comparable) || !(value2 instanceof Comparable)) {
                            throw new IllegalArgumentException("Between operator requires comparable types");
                        }
                        return cb.between((Expression<Comparable>) field, (Comparable) value1, (Comparable) value2);
                    }
                    throw new IllegalArgumentException("Between operator requires exactly 2 values separated by ':'");
                default:
                    return cb.equal(field, value);
            }
        } catch (IllegalArgumentException e) {
            throw e; // Re-throw validation errors
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid filter: " + filter.getField() + " " + filter.getOperator() + " "
                    + filter.getValue() + " - " + e.getMessage());
        }
    }

    /**
     * Get field path, handling nested fields (e.g., "team.name")
     */
    private Path<?> getFieldPath(Root<?> root, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    /**
     * Parse value based on field type
     */
    private Object parseValue(Class<?> fieldType, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            if (fieldType == String.class) {
                return value;
            } else if (fieldType == Long.class || fieldType == long.class) {
                return Long.parseLong(value);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return Integer.parseInt(value);
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (fieldType == LocalDate.class) {
                return LocalDate.parse(value);
            } else if (fieldType == LocalDateTime.class) {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (fieldType == Instant.class) {
                return Instant.parse(value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse value '" + value + "' as type " + fieldType.getSimpleName());
        }

        // For entity types or unknown types, return as string (will be handled by JPA)
        return value;
    }

    /**
     * Parse list value for IN operator
     */
    private List<Object> parseListValue(Class<?> fieldType, String value) {
        String[] values = value.split("\\|");
        List<Object> result = new ArrayList<>();
        for (String v : values) {
            result.add(parseValue(fieldType, v.trim()));
        }
        return result;
    }

    /**
     * Build Sort from query parameters
     */
    private Sort buildSort(QueryParams params) {
        List<Sort.Order> orders = new ArrayList<>();

        for (QueryParams.SortParam sortParam : params.getSortParams()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortParam.getDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, sortParam.getField()));
        }

        // Default sort by id if no sort specified
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        }

        return Sort.by(orders);
    }
}
