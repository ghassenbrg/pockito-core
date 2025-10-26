package io.ghassen.pockito.web.validation;

/**
 * Validation groups for different operations.
 * 
 * Used to apply different validation rules based on the operation being performed.
 */
public class ValidationGroups {
    
    /**
     * Validation group for create operations.
     * Used when creating new entities.
     */
    public interface Create {}
    
    /**
     * Validation group for update operations.
     * Used when updating existing entities.
     */
    public interface Update {}
}
