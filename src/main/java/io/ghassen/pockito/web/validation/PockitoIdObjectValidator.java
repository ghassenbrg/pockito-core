package io.ghassen.pockito.web.validation;

import jakarta.persistence.Id;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * Class-level validator for {@link PockitoId} used when the annotation is applied to an entity type.
 *
 * It locates the entity's @Id field via reflection and validates its String value
 * against the expected pattern ^PREFIX-\d{10,20}$ where PREFIX comes from the annotation.
 */
public class PockitoIdObjectValidator implements ConstraintValidator<PockitoId, Object> {

    private String prefix;
    private java.util.regex.Pattern pattern;

    @Override
    public void initialize(PockitoId annotation) {
        this.prefix = annotation.prefix();

        String escapedPrefix = java.util.regex.Pattern.quote(prefix);
        String patternString = "^" + escapedPrefix + "-\\d{10,20}$";
        this.pattern = java.util.regex.Pattern.compile(patternString);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // Find @Id field
        Field idField = findIdField(value.getClass());
        if (idField == null) {
            return true; // Not applicable
        }

        try {
            Object idVal = idField.get(value);
            if (idVal == null) {
                return true; // Allow null; lifecycle may set it in @PrePersist
            }
            if (!(idVal instanceof String)) {
                return true; // Only validate String IDs
            }

            boolean matches = pattern.matcher((String) idVal).matches();
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("ID must match pattern: %s-{10-20 digits}", prefix)
                ).addPropertyNode(idField.getName()).addConstraintViolation();
            }
            return matches;
        } catch (IllegalAccessException e) {
            return true; // Can't access -> skip validation
        }
    }

    private static Field findIdField(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    f.setAccessible(true);
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
}


