package com.boilerplate.domain.common.vos;


import com.boilerplate.domain.common.exceptions.DomainException;

public abstract class StringValueObject extends ValueObject<String> {

    protected StringValueObject(String value) {
        super(value);
    }

    @Override
    protected String validate(String value) {
        if (value == null)
            throw createException(getClass().getSimpleName() + " cannot be null");

        var trimmed = value.trim();
        if (trimmed.isEmpty())
            throw createException(getClass().getSimpleName() + " cannot be empty");

        return customValidate(trimmed);
    }

    protected abstract String customValidate(String value);

    protected void checkLength(String value, int min, int max) {
        if (value.length() < min)
            throw createException("must have at least " + min + " characters");

        if (value.length() > max)
            throw createException("must have at most " + max + " characters");
    }

    protected abstract DomainException createException(String message);
}
