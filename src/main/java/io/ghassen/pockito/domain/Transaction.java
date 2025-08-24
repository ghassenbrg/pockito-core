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
@Table(name = "txn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Transaction extends AuditableEntity {

  public enum TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER;
  }

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "txn_type_enum")
  private TransactionType type;

  @Column(nullable = false, name = "occurred_at")
  private LocalDate occurredAt;

  private String note;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id")
  private Wallet wallet;

  @Column(name = "wallet_id", insertable = false, updatable = false)
  private UUID walletId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "category_id", insertable = false, updatable = false)
  private UUID categoryId;

  @Column(precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(length = 3, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_wallet_id")
  private Wallet fromWallet;

  @Column(name = "from_wallet_id", insertable = false, updatable = false)
  private UUID fromWalletId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_wallet_id")
  private Wallet toWallet;

  @Column(name = "to_wallet_id", insertable = false, updatable = false)
  private UUID toWalletId;

  @Column(precision = 18, scale = 2, name = "from_amount")
  private BigDecimal fromAmount;

  @Column(precision = 18, scale = 2, name = "to_amount")
  private BigDecimal toAmount;

  @Column(length = 3, name = "from_currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String fromCurrencyCode;

  @Column(length = 3, name = "to_currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String toCurrencyCode;

  @Column(precision = 20, scale = 10, name = "exchange_rate")
  private BigDecimal exchangeRate;

  @Column(name = "external_wallet_name")
  private String externalWalletName;
}
