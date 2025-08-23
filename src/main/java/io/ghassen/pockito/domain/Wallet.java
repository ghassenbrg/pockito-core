package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet")
@FilterDef(name = "archivedFilter", parameters = @ParamDef(name = "archived", type = Boolean.class))
@Filter(name = "archivedFilter", condition = "archived_at IS NULL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet extends AuditableEntity {

  public enum WalletType { SAVINGS, BANK_ACCOUNT, CASH, CREDIT_CARD, CUSTOM }
  public enum IconType { EMOJI, URL }

  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "icon_type")
  private IconType iconType;

  @Column(nullable = false, name = "icon_value")
  private String iconValue;

  @Column(length = 3, nullable = false, name = "currency_code")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currencyCode;

  @Column(length = 7)
  private String color;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WalletType type;

  @Column(nullable = false, precision = 18, scale = 2, name = "initial_balance")
  private BigDecimal initialBalance = BigDecimal.ZERO;

  @Column(nullable = false, name = "is_default")
  private boolean isDefault = false;

  @Column(precision = 18, scale = 2, name = "goal_amount")
  private BigDecimal goalAmount;

  @PrePersist
  @PreUpdate
  private void validateSavingsGoal() {
    if (type == WalletType.SAVINGS && goalAmount != null && goalAmount.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Savings goal amount must be non-negative");
    }
  }
}
