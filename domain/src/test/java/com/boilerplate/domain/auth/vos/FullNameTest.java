package com.boilerplate.domain.auth.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FullName Value Object Tests")
class FullNameTest {

    @Test
    @DisplayName("Should accept valid full name")
    void shouldAcceptValidName() {
        var name = FullName.of("João Silva");

        assertThat(name.getValue()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Should accept minimum allowed length")
    void shouldAcceptMinimumLength() {
        var name = FullName.of("Jo");

        assertThat(name.getValue()).isEqualTo("Jo");
    }

    @Test
    @DisplayName("Should accept maximum allowed length")
    void shouldAcceptMaximumLength() {
        var value = "A".repeat(100);
        var name = FullName.of(value);

        assertThat(name.getValue()).isEqualTo(value);
    }

    @Test
    @DisplayName("Should reject null full name")
    void shouldRejectNull() {
        assertThatThrownBy(() -> FullName.of(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should reject blank full name")
    void shouldRejectBlank() {
        assertThatThrownBy(() -> FullName.of("   "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should reject full name shorter than minimum length")
    void shouldRejectTooShort() {
        assertThatThrownBy(() -> FullName.of("J"))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should reject full name exceeding maximum length")
    void shouldRejectTooLong() {
        assertThatThrownBy(() -> FullName.of("A".repeat(101)))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should support value equality")
    void shouldSupportValueEquality() {
        var a = FullName.of("João Silva");
        var b = FullName.of("João Silva");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        var a = FullName.of("João Silva");
        var b = FullName.of("Maria Silva");

        assertThat(a).isNotEqualTo(b);
    }
}