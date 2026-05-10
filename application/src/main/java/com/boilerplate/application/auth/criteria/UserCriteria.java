package com.boilerplate.application.auth.criteria;

import com.boilerplate.application.common.annotations.CriteriaField;
import com.boilerplate.application.common.annotations.CriteriaField.Operator;
import com.boilerplate.application.common.repository.Criteria;

public record UserCriteria(
        @CriteriaField(value = "name", operator = Operator.LIKE) String name,
        @CriteriaField("role") String role,
        @CriteriaField("active") Boolean active
) implements Criteria { }
