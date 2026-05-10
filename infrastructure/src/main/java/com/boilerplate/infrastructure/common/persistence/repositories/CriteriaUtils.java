package com.boilerplate.infrastructure.common.persistence.repositories;

import com.boilerplate.application.common.annotations.CriteriaField;
import com.boilerplate.application.common.annotations.CriteriaField.Operator;
import jakarta.persistence.criteria.*;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CriteriaUtils {

    public static <E, C> Predicate[] buildPredicates(
            C criteria,
            Root<E> root,
            CriteriaBuilder cb
    ) {
        List<Predicate> predicates = new ArrayList<>();

        for (RecordComponent component : criteria.getClass().getRecordComponents()) {
            var annotation = component.getAnnotation(CriteriaField.class);
            if (annotation == null) continue;

            try {
                Object value = component.getAccessor().invoke(criteria);
                if (value == null) continue;

                Path<Object> path = resolvePath(root, annotation.value());
                predicates.add(buildPredicate(cb, path, value, annotation.operator()));

            } catch (Exception e) {
                throw new RuntimeException(
                        "error building predicate for " + component.getName(), e
                );
            }
        }

        return predicates.toArray(new Predicate[0]);
    }

    private static <E> Path<Object> resolvePath(Root<E> root, String field) {
        String[] parts = field.split("\\.");
        Path<Object> path = root.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }

        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildPredicate(
            CriteriaBuilder cb,
            Path<Object> path,
            Object value,
            Operator operator
    ) {
        return switch (operator) {
            case EQUAL -> cb.equal(path, value);

            case LIKE -> cb.like(
                    cb.lower(path.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%"
            );

            case IN -> {
                if (!(value instanceof Collection<?> collection))
                    throw new IllegalArgumentException(
                            "IN operator requires a Collection"
                    );
                yield path.in(collection);
            }

            case GREATER_THAN -> cb.greaterThan(
                    path.as(Comparable.class),
                    (Comparable) value
            );

            case LESS_THAN -> cb.lessThan(
                    path.as(Comparable.class),
                    (Comparable) value
            );

            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(
                    path.as(Comparable.class),
                    (Comparable) value
            );

            case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(
                    path.as(Comparable.class),
                    (Comparable) value
            );
        };
    }
}