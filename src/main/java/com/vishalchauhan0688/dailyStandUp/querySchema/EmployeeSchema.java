package com.vishalchauhan0688.dailyStandUp.querySchema;

import com.vishalchauhan0688.dailyStandUp.query.Direction;
import com.vishalchauhan0688.dailyStandUp.query.request.SortCondition;
import com.vishalchauhan0688.dailyStandUp.query.schema.FieldSchema;
import com.vishalchauhan0688.dailyStandUp.query.schema.Operator;
import com.vishalchauhan0688.dailyStandUp.query.schema.ResourceSchema;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmployeeSchema {
    private static ResourceSchema SCHEMA;
    static{
        Map<String, FieldSchema> fields = new HashMap<>();

        fields.put(
                "id",
                new FieldSchema("id", Long.class, Set.of(Operator.EQ, Operator.IN), true
                )
        );

        fields.put(
                "status",
                new FieldSchema(
                        "status",
                        String.class,
                        Set.of(Operator.EQ, Operator.IN),
                        false
                )
        );

        SCHEMA = new ResourceSchema(
                "employee",
                fields,
                20,
                100,
                List.of(
                        new SortCondition("createdAt", Direction.DESC),
                        new SortCondition("id", Direction.ASC)
                )

        );
    }

    private EmployeeSchema(){}
}
