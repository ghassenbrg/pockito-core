package io.ghassen.pockito.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for Transaction ID format.
 * 
 * A composed constraint that uses {@link PockitoId} with prefix "TXN".
 * Validates that the value matches the pattern: ^TXN-\d{10,20}$
 * 
 * Usage example:
 * <pre>
 * {@code @TransactionId
 * private String transactionId;
 * }
 * </pre>
 */
@Documented
@PockitoId(prefix = "TXN", message = "Transaction ID must match pattern: TXN-{10-20 digits}")
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionId {
}

