package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Subscription extends AuditableEntity {

  public enum FrequencyType {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY,
    CUSTOM;
  }

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(columnDefinition = "icon_type_enum")
  private Wallet.IconType iconType;

  @Column(name = "icon_value")
  private String iconValue;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id")
  private Wallet wallet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "freq_type_enum")
  private FrequencyType frequency;

  @Column(nullable = false)
  private Integer interval = 1;

  @Column(name = "day_of_month")
  private Short dayOfMonth;

  @Column(name = "day_of_week")
  private Short dayOfWeek;

  @Column(name = "month_of_year")
  private Short monthOfYear;

  @Column(nullable = false, name = "start_date")
  private LocalDate startDate;

  @Column(name = "next_due_date")
  private LocalDate nextDueDate;
}
