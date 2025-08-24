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
@Table(name = "subscription_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SubscriptionPayment extends AuditableEntity {

  public enum PaymentStatus {
    PAID,
    SKIPPED,
    FAILED;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;

  @Column(name = "subscription_id", insertable = false, updatable = false)
  private UUID subscriptionId;

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

  @Column(nullable = false)
  private Boolean auto = true;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "payment_status_enum")
  private PaymentStatus status = PaymentStatus.PAID;
}
