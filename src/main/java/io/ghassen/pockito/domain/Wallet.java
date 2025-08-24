package io.ghassen.pockito.domain;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends AuditableEntity {

  public enum WalletType {
    SAVINGS,
    BANK_ACCOUNT,
    CASH,
    CREDIT_CARD,
    CUSTOM;
  }

  public enum IconType {
    EMOJI,
    URL;
  }

  @Column(nullable = false, name = "user_id")
  private String userId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, name = "icon_type", columnDefinition = "icon_type_enum")
  private IconType iconType;

  @Column(nullable = false, name = "icon_value")
  private String iconValue;

  @Column(length = 3, nullable = false, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @Column(length = 7)
  private String color;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "wallet_type_enum")
  private WalletType type;

  @Column(nullable = false, precision = 18, scale = 2, name = "initial_balance")
  private BigDecimal initialBalance = BigDecimal.ZERO;

  @Column(nullable = false, name = "is_default")
  private boolean isDefault = false;

  @Column(precision = 18, scale = 2, name = "goal_amount")
  private BigDecimal goalAmount;

  @Column(nullable = false, name = "display_order")
  private Integer displayOrder;

  @PrePersist
  @PreUpdate
  private void validateAndPrepare() {
    // Validate savings goal
    if (type == WalletType.SAVINGS && goalAmount != null && goalAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Savings goal amount must be non-negative");
    }
    
    // Set display order if not provided
    if (displayOrder == null) {
      // This will be set by the service layer before saving
      displayOrder = 1;
    }
  }
}
