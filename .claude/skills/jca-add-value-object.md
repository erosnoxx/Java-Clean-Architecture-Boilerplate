---
name: jca-add-value-object
description: Use when creating a Value Object in any domain — covers StringValueObject, BigDecimalValueObject, and generic ValueObject with examples and test checklist.
---

# Creating a Value Object

## Which Base to Extend

| Scenario | Extend |
|---|---|
| String with validation (name, email, code...) | `StringValueObject` |
| Decimal with scale/rounding (price, quantity...) | `BigDecimalValueObject` |
| Any other type (UUID-based ID, Integer, custom) | `ValueObject<T>` directly |

Package: `domain/src/main/java/com/<project>/<bc>/vos/<Name>.java`

Shared VOs used across BCs: `domain/src/main/java/com/<project>/domain/shared/vos/`

---

## StringValueObject

Base class handles automatically: null → throws `DomainException`, blank → throws, trims whitespace.

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.StringValueObject;

public final class ProductName extends StringValueObject {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    private ProductName(String value) { super(value); }

    public static ProductName of(String value) { return new ProductName(value); }

    @Override
    protected void customValidate(String value) {
        checkLength(value, MIN_LENGTH, MAX_LENGTH);
        // add format checks here if needed
    }
}
```

Override `createException(String)` to customize the exception type or message prefix:
```java
@Override
protected DomainException createException(String message) {
    return new DomainException("ProductName: " + message);
}
```

---

## BigDecimalValueObject

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.BigDecimalValueObject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Price extends BigDecimalValueObject {

    private Price(BigDecimal value) { super(value); }

    public static Price of(BigDecimal value) { return new Price(value); }

    @Override protected int scale() { return 2; }
    @Override protected RoundingMode roundingMode() { return RoundingMode.HALF_EVEN; }
    @Override protected String type() { return "price"; }
    @Override protected boolean allowNegative() { return false; }
}
```

**Important:** arithmetic methods return `BigDecimal`, not the VO subtype.

```java
// CORRECT
BigDecimal subtotal = price.multiply(new BigDecimal("3"));
BigDecimal discounted = price.subtract(discount);

// WRONG — these methods don't exist on the subtype directly
Price result = price.add(otherPrice); // ❌
```

Available methods: `add(other)`, `subtract(other)`, `multiply(BigDecimal)`, `divide(BigDecimal)`, `isZero()`, `isPositive()`, `isNegative()`, `isGreaterThan(other)`, `isLessThan(other)`, `isGreaterThanOrEqualTo(other)`, `isLessThanOrEqualTo(other)`.

---

## ValueObject<T> — Generic

```java
package com.<project>.<bc>.vos;

import com.<project>.domain.common.vos.ValueObject;
import com.<project>.domain.common.exceptions.DomainException;

public final class OrderNumber extends ValueObject<String> {

    private OrderNumber(String value) { super(value); }

    public static OrderNumber of(String value) { return new OrderNumber(value); }

    @Override
    protected String validate(String value) {
        if (value == null || !value.matches("^[A-Z]+-\\d{6}-\\d{4}$"))
            throw new DomainException("invalid order number format");
        return value;
    }
}
```

---

## Test Checklist

```java
@Test void validValue() {
    var vo = ProductName.of("Widget Pro");
    assertThat(vo.getValue()).isEqualTo("Widget Pro");
}

@Test void nullRejected() {
    assertThatThrownBy(() -> ProductName.of(null))
        .isInstanceOf(DomainException.class);
}

@Test void blankRejected() {
    assertThatThrownBy(() -> ProductName.of("   "))
        .isInstanceOf(DomainException.class);
}

@Test void tooShortRejected() {
    assertThatThrownBy(() -> ProductName.of("A"))
        .isInstanceOf(DomainException.class);
}

@Test void equalityByValue() {
    assertThat(ProductName.of("Widget")).isEqualTo(ProductName.of("Widget"));
    assertThat(ProductName.of("Widget")).isNotEqualTo(ProductName.of("Gadget"));
}

@Test void trimmingApplied() {
    assertThat(ProductName.of("  Widget  ").getValue()).isEqualTo("Widget");
}
```
