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
import java.util.UUID;

@MappedSuperclass
@Getter @Setter @SuperBuilder @NoArgsConstructor
public abstract class AuditableEntity extends AuditableEntityNoId {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

}
