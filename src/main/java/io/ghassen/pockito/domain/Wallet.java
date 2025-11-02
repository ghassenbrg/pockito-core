package io.ghassen.pockito.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.WalletType;
import io.ghassen.pockito.domain.validation.WalletId;

/**
 * Wallet entity representing a user's financial account or instrument.
 * 
 * Each wallet belongs to a specific user and can be categorized by type.
 * Wallets support various features like initial balance, goals, and customization options.
 * 
 * Entity behavior:
 * - Each user can have multiple wallets
 * - Wallet names must be unique per user
 * - Only one wallet per user can be marked as default
 * - Order position determines display order
 * - Money fields are normalized to 2 decimal places
 */
@Entity
@Table(
    name = "t_wallet",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"}, name = "uk_wallet_user_name")
    },
    indexes = {
        @Index(columnList = "user_id", name = "idx_wallet_user_id"),
        @Index(columnList = "is_default", name = "idx_wallet_is_default"),
        @Index(columnList = "type", name = "idx_wallet_type"),
        @Index(columnList = "order_position", name = "idx_wallet_order_position")
    }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@WalletId
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Wallet extends AuditableEntity {

    /**
     * The user who owns this wallet.
     * Required relationship, cannot be null.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "username",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_wallet_user")
    )
    @NotNull
    private User user;

    /**
     * Wallet name - must be unique per user.
     * Required field with length validation.
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    /**
     * Initial balance of the wallet.
     * Required field with precision validation (17,2).
     */
    @Column(name = "initial_balance", nullable = false, precision = 17, scale = 2)
    @NotNull
    @Digits(integer = 15, fraction = 2)
    private BigDecimal initialBalance;

    /**
     * Currency code for the wallet.
     * Required field using ISO-4217 standard.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    @NotNull
    private CurrencyCode currency;

    /**
     * URL to the wallet's icon/image.
     * Optional field with length validation.
     */
    @Column(name = "icon_url", length = 500)
    @Size(max = 500)
    private String iconUrl;

    /**
     * Goal amount for the wallet (e.g., savings target).
     * Optional field with precision validation (17,2).
     */
    @Column(name = "goal_amount", precision = 17, scale = 2)
    @Digits(integer = 15, fraction = 2)
    private BigDecimal goalAmount;

    /**
     * Type/category of the wallet.
     * Required field using predefined enum values.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull
    private WalletType type;

    /**
     * Whether this wallet is the user's default wallet.
     * Required field, defaults to false.
     */
    @Column(name = "is_default", nullable = false)
    @NotNull
    private Boolean isDefault = false;

    /**
     * Display order position for the wallet.
     * Required field, must be non-negative.
     */
    @Column(name = "order_position", nullable = false)
    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private Integer orderPosition;

    /**
     * Description of the wallet.
     * Optional field with length validation.
     */
    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;

    /**
     * Hex color code for the wallet (e.g., #A1B2C3).
     * Optional field with hex color validation.
     */
    @Column(name = "color", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #A1B2C3)")
    @Size(min = 7, max = 7)
    private String color;
}
