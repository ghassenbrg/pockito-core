package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agreement_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AgreementPayment extends AuditableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agreement_id", nullable = false)
  private Agreement agreement;

  @Column(name = "agreement_id", insertable = false, updatable = false)
  private UUID agreementId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "txn_id")
  private Transaction transaction;

  @Column(name = "txn_id", insertable = false, updatable = false)
  private UUID transactionId;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @Column(nullable = false, name = "paid_at")
  private Instant paidAt = Instant.now();

  private String note;
}
