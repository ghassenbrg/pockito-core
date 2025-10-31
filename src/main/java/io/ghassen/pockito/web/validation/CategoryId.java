package io.ghassen.pockito.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for Category ID format.
 * 
 * A composed constraint that uses {@link PockitoId} with prefix "CAT".
 * Validates that the value matches the pattern: ^CAT-\d{10,20}$
 * 
 * Usage example:
 * <pre>
 * {@code @CategoryId
 * private String categoryId;
 * }
 * </pre>
 */
@Documented
@PockitoId(prefix = "CAT", message = "Category ID must match pattern: CAT-{10-20 digits}")
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CategoryId {
}

