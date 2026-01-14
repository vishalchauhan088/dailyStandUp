package com.vishalchauhan0688.dailyStandUp.fieldPathResolver;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public interface FieldPathResolver<T> {
    Path<?> resolve(Root<T> root, String field);
}
