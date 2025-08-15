package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "currency")
@SQLRestriction("archived_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Currency extends AuditableEntityNoId {

  @Id
  @Column(length = 3, nullable = false)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String code;

  @Column(nullable = false)
  private String name;

  private String symbol;

  @Column(nullable = false)
  private Short decimals = 2;

  @Column(nullable = false, name = "is_active")
  private Boolean isActive = true;
}
