package com.boilerplate.domain.shared.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name+tag@domain.co.uk",
            "user-name@sub.domain.org",
            "u@x.io",
            "USER@EXAMPLE.COM"
    })
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmails(String value) {
        var email = Email.of(value);

        assertThat(email.getValue()).isEqualTo(value.toLowerCase());
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeToLowerCase() {
        var email = Email.of("User.Name@Domain.COM");

        assertThat(email.getValue()).isEqualTo("user.name@domain.com");
    }

    @Test
    @DisplayName("Should reject null email")
    void shouldRejectNull() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should reject blank email")
    void shouldRejectBlank() {
        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Should reject email exceeding maximum length")
    void shouldRejectExceedingMaxLength() {
        var localPart = "a".repeat(244);
        var value = localPart + "@example.com";

        assertThatThrownBy(() -> Email.of(value))
                .isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "notanemail",
            "@domain.com",
            "user@",
            "user@domain",
            "user @domain.com",
            "user@domain..com",
            "user@.com"
    })
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidFormats(String value) {
        assertThatThrownBy(() -> Email.of(value))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("invalid email format");
    }

    @Test
    @DisplayName("Should support value equality")
    void shouldSupportValueEquality() {
        var a = Email.of("user@example.com");
        var b = Email.of("USER@EXAMPLE.COM");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when email values differ")
    void shouldNotBeEqualWhenValuesDiffer() {
        var a = Email.of("a@example.com");
        var b = Email.of("b@example.com");

        assertThat(a).isNotEqualTo(b);
    }
}