package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "exchange_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExchangeRate extends AuditableEntity {

  @Column(nullable = false, name = "base_code", length = 3)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String baseCode;

  @Column(nullable = false, name = "quote_code", length = 3)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String quoteCode;

  @Column(nullable = false, precision = 20, scale = 10)
  private BigDecimal rate;

  @Column(nullable = false, name = "effective_at")
  private Instant effectiveAt;

  private String source;
}
