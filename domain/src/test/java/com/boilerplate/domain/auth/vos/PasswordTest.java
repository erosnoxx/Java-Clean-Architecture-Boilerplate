package com.boilerplate.domain.auth.vos;

import com.boilerplate.domain.common.exceptions.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Value Object Tests")
class PasswordTest {

    @Test
    @DisplayName("Should create raw password successfully")
    void shouldCreateRawPasswordSuccessfully() {
        Password password = Password.fromRaw("StrongPass123");

        assertNotNull(password);
        assertEquals("StrongPass123", password.getValue());
        assertFalse(password.isHashed());
    }

    @Test
    @DisplayName("Should create hashed password successfully")
    void shouldCreateHashedPasswordSuccessfully() {
        String hashedValue = "$2a$10$7EqJtq98hPqEX7fNZaFWoO";

        Password password = Password.fromHashed(hashedValue);

        assertNotNull(password);
        assertEquals(hashedValue, password.getValue());
        assertTrue(password.isHashed());
    }

    @Test
    @DisplayName("Should throw exception when raw password is null")
    void shouldThrowExceptionWhenRawPasswordIsNull() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Password.fromRaw(null)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when raw password is blank")
    void shouldThrowExceptionWhenRawPasswordIsBlank() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Password.fromRaw(" ")
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when raw password is shorter than minimum length")
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Password.fromRaw("1234567")
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should allow raw password with exactly minimum length")
    void shouldAllowPasswordWithMinimumLength() {
        Password password = Password.fromRaw("12345678");

        assertNotNull(password);
        assertEquals("12345678", password.getValue());
    }

    @Test
    @DisplayName("Should throw exception when raw password exceeds maximum length")
    void shouldThrowExceptionWhenPasswordExceedsMaximumLength() {
        String oversizedPassword = "a".repeat(129);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Password.fromRaw(oversizedPassword)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should allow raw password with exactly maximum length")
    void shouldAllowPasswordWithMaximumLength() {
        String validPassword = "a".repeat(128);

        Password password = Password.fromRaw(validPassword);

        assertNotNull(password);
        assertEquals(validPassword, password.getValue());
    }

    @Test
    @DisplayName("Should not validate length for hashed passwords")
    void shouldNotValidateLengthForHashedPasswords() {
        Password password = Password.fromHashed("123");

        assertNotNull(password);
        assertEquals("123", password.getValue());
        assertTrue(password.isHashed());
    }

    @Test
    @DisplayName("Passwords with same value should be equal")
    void shouldBeEqualWhenValuesAreSame() {
        Password password1 = Password.fromRaw("StrongPass123");
        Password password2 = Password.fromRaw("StrongPass123");

        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }

    @Test
    @DisplayName("Passwords with different values should not be equal")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        Password password1 = Password.fromRaw("StrongPass123");
        Password password2 = Password.fromRaw("AnotherPass456");

        assertNotEquals(password1, password2);
    }
}