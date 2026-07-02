package com.boilerplate.domain.common.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class BigDecimalValueObject extends ValueObject<BigDecimal> {

    protected BigDecimalValueObject(BigDecimal value) {
        super(value);
    }

    protected abstract int scale();
    protected abstract RoundingMode roundingMode();
    protected abstract String type();
    protected abstract boolean allowNegative();

    @Override
    protected BigDecimal validate(BigDecimal value) {
        if (value == null) throw new DomainException(type() + " must not be null");
        if (!allowNegative() && value.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException(type() + " must not be negative");
        return value.setScale(scale(), roundingMode());
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThan(BigDecimalValueObject other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(BigDecimalValueObject other) {
        return this.value.compareTo(other.value) < 0;
    }

    public boolean isGreaterThanOrEqualTo(BigDecimalValueObject other) {
        return this.value.compareTo(other.value) >= 0;
    }

    public boolean isLessThanOrEqualTo(BigDecimalValueObject other) {
        return this.value.compareTo(other.value) <= 0;
    }

    public BigDecimal add(BigDecimalValueObject other) {
        return this.value.add(other.value).setScale(scale(), roundingMode());
    }

    public BigDecimal subtract(BigDecimalValueObject other) {
        return this.value.subtract(other.value).setScale(scale(), roundingMode());
    }

    public BigDecimal multiply(BigDecimal factor) {
        return this.value.multiply(factor).setScale(scale(), roundingMode());
    }

    public BigDecimal divide(BigDecimal divisor) {
        return this.value.divide(divisor, scale(), roundingMode());
    }
}
