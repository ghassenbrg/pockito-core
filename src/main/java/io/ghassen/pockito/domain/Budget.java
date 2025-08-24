package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budget")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Budget extends AuditableEntity {

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "freq_type_enum")
  private Subscription.FrequencyType period = Subscription.FrequencyType.MONTHLY;

  @Column(nullable = false, name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(nullable = false, precision = 18, scale = 2, name = "limit_amount")
  private BigDecimal limitAmount;

  @Column(nullable = false, length = 3, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;
}
