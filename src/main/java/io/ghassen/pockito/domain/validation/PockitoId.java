package io.ghassen.pockito.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating Pockito ID format.
 * 
 * Uses pattern validation (similar to {@link jakarta.validation.constraints.Pattern})
 * to ensure the value matches: ^PREFIX-\d{10,20}$
 * where PREFIX is a configurable 3-character prefix value.
 * 
 * A custom validator is used instead of composing {@link jakarta.validation.constraints.Pattern}
 * because the regex pattern must be dynamically constructed based on the prefix parameter.
 * 
 * Usage example:
 * <pre>
 * {@code @PockitoId(prefix = "TXN")
 * private String transactionId;
 * }
 * </pre>
 */
@Documented
@Constraint(validatedBy = {PockitoIdValidator.class, PockitoIdObjectValidator.class})
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PockitoId {
    
    /**
     * The prefix value to use in the validation pattern.
     * Must be exactly 3 characters.
     * Defaults to "PCK".
     * 
     * @return the prefix string (must be 3 characters)
     */
    String prefix() default "PCK";
    
    /**
     * Validation error message.
     * 
     * @return the error message
     */
    String message() default "ID must match pattern: {prefix}-{10-20 digits}";
    
    /**
     * Validation groups.
     * 
     * @return validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for carrying metadata information.
     * 
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
}

