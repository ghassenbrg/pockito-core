package io.ghassen.pockito.domain;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base entity class that provides ID field for entities using generated IDs.
 * 
 * ID generation is handled in AuditableEntityNoId using @PrePersist hook.
 * The ID prefix is determined by class-level composed annotations (e.g., @CategoryId, @TransactionId)
 * or field-level annotations on the concrete entity. No default class-level prefix is enforced here.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntity extends AuditableEntityNoId {

  /**
   * Entity ID field.
   * ID is auto-generated on persist using @PrePersist hook in AuditableEntityNoId
   * based on class-level or field-level annotations.
   */
  @Id
  @Column(name = "id", length = 30)
  protected String id;

}
