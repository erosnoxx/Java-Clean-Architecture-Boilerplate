package com.boilerplate.application.auth.criteria;

import com.boilerplate.application.common.annotations.CriteriaField;
import com.boilerplate.application.common.annotations.CriteriaField.Operator;
import com.boilerplate.application.common.repository.Criteria;
import com.boilerplate.domain.auth.enums.UserStatus;

public record UserCriteria(
        @CriteriaField(value = "name", operator = Operator.LIKE) String name,
        @CriteriaField("role") String role,
        @CriteriaField("status") UserStatus status
) implements Criteria { }
