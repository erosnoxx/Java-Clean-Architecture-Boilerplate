package com.boilerplate.domain.common.vos;

import lombok.Getter;

import java.util.Objects;

@Getter
public abstract class ValueObject<T> {
    protected final T value;

    protected ValueObject(T value) {
        this.value = validate(value);
    }

    protected abstract T validate(T value);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValueObject<?> that = (ValueObject<?>) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}
