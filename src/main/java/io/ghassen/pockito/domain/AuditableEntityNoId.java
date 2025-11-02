package io.ghassen.pockito.domain;

import java.lang.reflect.Field;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import io.ghassen.pockito.domain.util.PockitoIdGenerator;
import io.ghassen.pockito.shared.security.SecurityUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntityNoId {

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt;

  @CreatedBy
  @Column(name = "created_by")
  private String createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @Column(name = "archived_by")
  private String archivedBy;

  @Version
  private Long version;

  /**
   * Indicates whether the last create or update operation was performed by the
   * system (not a user).
   * This is a transient field and is not persisted to the database.
   */
  @Transient
  private boolean systemAction = false;

  /**
   * Pre-persist callback that handles both ID generation and audit field setting.
   * Generates the Pockito ID before persisting the entity if it extends AuditableEntity.
   * The ID is generated based on class-level or field-level annotations.
   * First checks for class-level @PockitoId composed annotations (e.g., @CategoryId),
   * then falls back to field-level annotations (e.g., @CategoryId on id field),
   * and finally to @PockitoId on AuditableEntity class.
   * Also sets the createdBy field with the current user or "system" if systemAction is true.
   */
  @PrePersist
  private void prePersistCallback() {
    // Set createdBy field
    if (systemAction) {
      this.setCreatedBy("system");
    } else {
      this.setCreatedBy(SecurityUtils.getCurrentUserId());
    }
    
    // Generate ID if needed (only if entity extends AuditableEntity)
    try {
      // Use reflection to find the actual id field in the concrete class
      String currentId = getIdViaReflection();

      // Only generate if ID is not already set and if @PockitoId annotation exists
      if (currentId == null || currentId.isEmpty()) {
        String username = systemAction ? "system" : SecurityUtils.getCurrentUserId();
        String generatedId = PockitoIdGenerator.generateId(this, username);
        
        // Only set the ID if generation was successful (annotation found)
        // If no @PockitoId annotation, generatedId will be null and we skip ID generation
        if (generatedId != null) {
          setIdViaReflection(generatedId);
        }
        // If generatedId is null, no @PockitoId annotation was found, skip silently
      }
    } catch (Exception e) {
      // If exception occurs (e.g., reflection issue), log but don't fail the persist
      // The entity might use another ID generation strategy
      // Could optionally log a warning here if needed
    }
  }

  /**
   * Finds the field annotated with @Id in the entity hierarchy.
   * Starts from the concrete class and walks up the hierarchy if needed.
   * 
   * @return the Field annotated with @Id, or null if not found
   */
  private Field findIdField() {
    Class<?> clazz = this.getClass();
    // Start from the concrete class (child) and walk up to parent
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Id.class)) {
          field.setAccessible(true);
          return field;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null; // Shouldn't happen if entity extends AuditableEntity
  }

  /**
   * Gets the ID value using reflection to find the field annotated with @Id.
   * This ensures we access the correct field even when child classes override it.
   */
  private String getIdViaReflection() throws IllegalAccessException {
    Field idField = findIdField();
    if (idField != null) {
      Object idValue = idField.get(this);
      return idValue != null ? idValue.toString() : null;
    }
    return null;
  }

  /**
   * Sets the ID value using reflection to find the field annotated with @Id.
   * This ensures we set the correct field even when child classes override it.
   */
  private void setIdViaReflection(String id) throws IllegalAccessException {
    Field idField = findIdField();
    if (idField != null) {
      idField.set(this, id);
    }
  }


  @PreUpdate
  private void setUpdatedByUserId() {
    if (systemAction) {
      this.setUpdatedBy("system");
      return;
    }
    this.setUpdatedBy(SecurityUtils.getCurrentUserId());
  }

}
