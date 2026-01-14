package com.vishalchauhan0688.dailyStandUp.query.translation;

import com.vishalchauhan0688.dailyStandUp.fieldPathResolver.FieldPathResolver;
import com.vishalchauhan0688.dailyStandUp.query.normalization.NormalizedQuery;
import com.vishalchauhan0688.dailyStandUp.query.normalization.QueryNormalizer;
import com.vishalchauhan0688.dailyStandUp.query.request.FilterCondition;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class SpecificationBuilder {

    public <T> Specification<T> buildSpecification(
            NormalizedQuery nq,
            FieldPathResolver<T> pathresolver
    ) {
        return (root, query, cb) ->{
            List<Predicate> predicates = new ArrayList<>();
            for(FilterCondition fc : nq.getFilters()){
                Path<?> path = pathresolver.resolve(root,fc.getField());

                Predicate p = switch (fc.getOperator()) {
                    case EQ -> cb.equal(path, fc.getValues());
                    case GT -> cb.greaterThan(
                            path.as(Comparable.class),
                            (Comparable) fc.getValues()
                    );
                    case GTE -> cb.greaterThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) fc.getValues()
                    );
                    case LT -> cb.lessThan(
                            path.as(Comparable.class),
                            (Comparable) fc.getValues()
                    );
                    case LTE -> cb.lessThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) fc.getValues()
                    );
                    default -> throw new IllegalStateException(
                            "Unsupported operator"
                    );
                };
                predicates.add(p);
            }

            return cb.and(predicates.toArray(new Predicate[0]));

        };
    }
}
