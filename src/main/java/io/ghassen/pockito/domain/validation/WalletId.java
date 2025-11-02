package io.ghassen.pockito.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for Wallet ID format.
 * 
 * A composed constraint that uses {@link PockitoId} with prefix "WAL".
 * Validates that the value matches the pattern: ^WAL-\d{10,20}$
 * 
 * Usage example:
 * <pre>
 * {@code @WalletId
 * private String walletId;
 * }
 * </pre>
 */
@Documented
@PockitoId(prefix = "WAL", message = "Wallet ID must match pattern: WAL-{10-20 digits}")
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WalletId {
}

