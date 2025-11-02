package io.ghassen.pockito.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link PockitoId} annotation.
 * 
 * Implements pattern validation (similar to {@link jakarta.validation.constraints.Pattern})
 * to ensure the value matches: ^PREFIX-\d{10,20}$
 * where PREFIX is a 3-character prefix taken from the annotation's prefix parameter.
 * 
 * A custom validator is required instead of using {@link jakarta.validation.constraints.Pattern}
 * directly because the regex pattern must be dynamically constructed based on the prefix parameter
 * at runtime.
 */
public class PockitoIdValidator implements ConstraintValidator<PockitoId, String> {
    
    private String prefix;
    private java.util.regex.Pattern pattern;
    
    @Override
    public void initialize(PockitoId annotation) {
        this.prefix = annotation.prefix();
        
        // Validate that prefix is exactly 3 characters
        if (prefix == null || prefix.length() != 3) {
            throw new IllegalArgumentException(
                "PockitoId prefix must be exactly 3 characters. Got: " + 
                (prefix == null ? "null" : "'" + prefix + "' (" + prefix.length() + " characters)")
            );
        }
        
        // Escape special regex characters in the prefix and build the pattern
        String escapedPrefix = java.util.regex.Pattern.quote(prefix);
        String patternString = "^" + escapedPrefix + "-\\d{10,20}$";
        this.pattern = java.util.regex.Pattern.compile(patternString);
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are considered valid (use @NotNull if null should be invalid)
        if (value == null) {
            return true;
        }
        
        // Validate against the pattern
        boolean matches = pattern.matcher(value).matches();
        
        // Customize error message with the prefix
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("ID must match pattern: %s-{10-20 digits}", prefix)
            ).addConstraintViolation();
        }
        
        return matches;
    }
}

