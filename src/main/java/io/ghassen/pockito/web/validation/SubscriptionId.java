package io.ghassen.pockito.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for Subscription ID format.
 * 
 * A composed constraint that uses {@link PockitoId} with prefix "SUB".
 * Validates that the value matches the pattern: ^SUB-\d{10,20}$
 * 
 * Usage example:
 * <pre>
 * {@code @SubscriptionId
 * private String subscriptionId;
 * }
 * </pre>
 */
@Documented
@PockitoId(prefix = "SUB", message = "Subscription ID must match pattern: SUB-{10-20 digits}")
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscriptionId {
}

