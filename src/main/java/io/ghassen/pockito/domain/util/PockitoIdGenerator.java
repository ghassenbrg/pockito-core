package io.ghassen.pockito.domain.util;

import io.ghassen.pockito.web.validation.PockitoId;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.Id;

/**
 * Utility class for generating Pockito IDs based on entity annotations.
 * 
 * Generates IDs in the format: PREFIX-{timestamp}{usernameHash}{counter}
 * where:
 * - PREFIX is extracted from the @PockitoId annotation (or composed annotations like @TransactionId)
 * - timestamp: last 10 digits of epoch milliseconds (for higher entropy)
 * - usernameHash: 5 digits from username hashCode
 * - counter: 4 digits, process-local counter to avoid collisions within the same millisecond
 */
public class PockitoIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicInteger COUNTER = new AtomicInteger(RANDOM.nextInt(10000));

    public static String generateId(Object entity, String username) {
        String prefix = extractPrefix(entity);
        // If no @PockitoId annotation found, return null (skip ID generation)
        if (prefix == null) {
            return null;
        }
        
        long epochMillis = Instant.now().toEpochMilli();
        int hash = (username != null ? username : "system").hashCode();
        int counter = COUNTER.getAndIncrement();

        // Keep components within fixed digit lengths
        long timestampPart = epochMillis % 10000000000L; // 10 digits
        int hashPart = Math.abs(hash) % 100000;          // 5 digits
        int counterPart = Math.abs(counter) % 10000;     // 4 digits

        // Total digits: 10 + 5 + 4 = 19 (fits ^PREFIX-\d{10,20}$)
        return String.format("%s-%010d%05d%04d", prefix,
            timestampPart,
            hashPart,
            counterPart);
    }
    
    private static String extractPrefix(Object entity) {
        Class<?> clazz = entity.getClass();
        
        // First, check class-level annotations (child class has priority)
        while (clazz != null && clazz != Object.class) {
            // Check direct @PockitoId on the class
            if (clazz.isAnnotationPresent(PockitoId.class)) {
                return clazz.getAnnotation(PockitoId.class).prefix();
            }
            
            // Check composed annotations on the class (e.g., @CategoryId, @WalletId, @TransactionId)
            for (Annotation annotation : clazz.getAnnotations()) {
                PockitoId pockitoId = annotation.annotationType().getAnnotation(PockitoId.class);
                if (pockitoId != null) {
                    return pockitoId.prefix(); // Return immediately - child annotation takes priority
                }
            }
            
            clazz = clazz.getSuperclass();
        }
        
        // If no class-level annotation found, check field-level annotations
        // Find the field annotated with @Id
        Field idField = findIdField(entity.getClass());
        if (idField != null) {
            // Check direct @PockitoId on this field
            if (idField.isAnnotationPresent(PockitoId.class)) {
                return idField.getAnnotation(PockitoId.class).prefix();
            }
            
            // Check composed annotations on this field
            for (Annotation annotation : idField.getAnnotations()) {
                PockitoId pockitoId = annotation.annotationType().getAnnotation(PockitoId.class);
                if (pockitoId != null) {
                    return pockitoId.prefix(); // Return immediately - child annotation takes priority
                }
            }
        }
        
        // No @PockitoId annotation found - return null to skip ID generation
        return null;
    }
    
    /**
     * Finds the field annotated with @Id in the entity hierarchy.
     * Starts from the concrete class and walks up the hierarchy if needed.
     * 
     * @param entityClass the class to search in
     * @return the Field annotated with @Id, or null if not found
     */
    private static Field findIdField(Class<?> entityClass) {
        Class<?> clazz = entityClass;
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
