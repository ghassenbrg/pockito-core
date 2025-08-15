package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.Instant;

@MappedSuperclass
@Getter @Setter @SuperBuilder @NoArgsConstructor
public abstract class AuditableEntityNoId {

  @CreationTimestamp
  @Column(nullable = false, updatable = false, name = "created_at")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false, name = "updated_at")
  private Instant updatedAt;

  @CreatedBy @Column(name = "created_by")
  private String createdBy;

  @LastModifiedBy @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @Column(name = "archived_by")
  private String archivedBy;

  @Version
  private Long version;
}
