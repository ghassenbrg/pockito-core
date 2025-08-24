package io.ghassen.pockito.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

  @Column(nullable = false, name = "start_date")
  private LocalDate startDate = LocalDate.now();

  private String note;

  @Column(nullable = false)
  private String status = "OPEN";
}
