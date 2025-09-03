package io.ghassen.pockito.domain;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditableEntity extends AuditableEntityNoId {

  @Id
  @GeneratedValue
  @UuidGenerator
  private UUID id;

}
