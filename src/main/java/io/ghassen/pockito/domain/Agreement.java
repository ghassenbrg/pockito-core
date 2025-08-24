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
@Table(name = "agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Agreement extends AuditableEntity {

  public enum AgreementType {
    BORROW,
    LEND;
  }

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Column(nullable = false, name = "person_name")
  private String personName;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "agreement_type_enum")
  private AgreementType type;

  @Column(nullable = false, precision = 18, scale = 2, name = "principal_amount")
  private BigDecimal principalAmount;

  @Column(nullable = false, length = 3, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id")
  private Wallet wallet;

  @Column(name = "wallet_id", insertable = false, updatable = false)
  private UUID walletId;

  @Column(nullable = false, name = "start_date")
  private LocalDate startDate = LocalDate.now();

  private String note;

  @Column(nullable = false)
  private String status = "OPEN";
}
