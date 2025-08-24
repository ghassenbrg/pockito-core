package io.ghassen.pockito.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import io.ghassen.pockito.security.SecurityUtils;
import jakarta.persistence.Column;
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

  @PrePersist
  private void setCreatedByUserId() {
    if (systemAction) {
      this.setCreatedBy("system");
      return;
    }
    this.setCreatedBy(SecurityUtils.getCurrentUserId());
  }

  @PreUpdate
  private void setUpdatedByUserId() {
    if (systemAction) {
      this.setCreatedBy("system");
      return;
    }
    this.setCreatedBy(SecurityUtils.getCurrentUserId());
  }

}
