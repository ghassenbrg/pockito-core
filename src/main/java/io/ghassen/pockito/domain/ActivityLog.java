package io.ghassen.pockito.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityLog extends AuditableEntity {

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Column(nullable = false, name = "entity_type")
  private String entityType;

  @Column(nullable = false, name = "entity_id")
  private UUID entityId;

  @Column(nullable = false)
  private String action;

  @Column(columnDefinition = "JSONB")
  private String payload;
}
