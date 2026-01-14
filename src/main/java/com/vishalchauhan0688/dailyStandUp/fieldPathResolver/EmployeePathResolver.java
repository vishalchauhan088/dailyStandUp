package com.vishalchauhan0688.dailyStandUp.fieldPathResolver;

import com.vishalchauhan0688.dailyStandUp.model.Employee;
import com.vishalchauhan0688.dailyStandUp.querySchema.EmployeeSchema;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public class EmployeePathResolver implements FieldPathResolver<Employee> {

    @Override
    public Path<?> resolve(Root<Employee> root, String field) {
        return switch (field){
            case "id" -> root.get("id");
            case "firstname" -> root.get("firstname");
            case "createdAt" -> root.get("createdAt");
            default -> throw new IllegalArgumentException(
                    "Unknown field: " + field
            );
        };
    }
}
