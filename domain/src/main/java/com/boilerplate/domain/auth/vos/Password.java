package com.boilerplate.domain.auth.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import com.boilerplate.domain.common.vos.StringValueObject;

public final class Password extends StringValueObject {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private final boolean hashed;

    private Password(String value, boolean hashed) {
        super(validatePassword(value, hashed));
        this.hashed = hashed;
    }

    public static Password fromRaw(String value) {
        return new Password(value, false);
    }

    public static Password fromHashed(String value) {
        return new Password(value, true);
    }

    private static String validatePassword(String value, boolean hashed) {
        if (value == null)
            throw new DomainException("Password cannot be null");

        var trimmed = value.trim();

        if (trimmed.isEmpty())
            throw new DomainException("Password cannot be empty");

        if (!hashed && (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH))
            throw new DomainException("must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters");

        return trimmed;
    }

    public boolean isHashed() {
        return hashed;
    }
}
