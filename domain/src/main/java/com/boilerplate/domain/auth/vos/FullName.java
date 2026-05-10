package com.boilerplate.domain.auth.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import com.boilerplate.domain.common.vos.StringValueObject;

public final class FullName extends StringValueObject {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    private FullName(String value) {
        super(value);
    }

    public static FullName of(String value) {
        return new FullName(value);
    }

    @Override
    protected String customValidate(String value) {
        checkLength(value, MIN_LENGTH, MAX_LENGTH);
        return value;
    }

    @Override
    protected DomainException createException(String message) {
        return new DomainException(message);
    }
}