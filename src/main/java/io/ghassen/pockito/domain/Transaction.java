package io.ghassen.pockito.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction entity representing a financial transaction between wallets.
 * 
 * Transactions can be of three types: TRANSFER, EXPENSE, or INCOME.
 * Each transaction type has specific requirements for wallet relationships and categories.
 * 
 * Entity behavior:
 * - TRANSFER: Requires both walletFrom and walletTo (one can be NULL for external transfers)
 * - EXPENSE: Requires walletFrom, category is optional but recommended
 * - INCOME: Requires walletTo, category is optional but recommended
 * - Exchange rate defaults to 1.0 and is used to calculate walletToAmount
 * - Effective date determines when the transaction takes effect
 * - Note field allows for additional transaction details
 */
@Entity
@Table(
    name = "t_transaction",
    indexes = {
        @Index(columnList = "user_id", name = "idx_transaction_user_id"),
        @Index(columnList = "transaction_type", name = "idx_transaction_type"),
        @Index(columnList = "effective_date", name = "idx_transaction_effective_date"),
        @Index(columnList = "wallet_from_id", name = "idx_transaction_wallet_from"),
        @Index(columnList = "wallet_to_id", name = "idx_transaction_wallet_to"),
        @Index(columnList = "category_id", name = "idx_transaction_category")
    }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Transaction extends AuditableEntity {

    /**
     * The user who owns this transaction.
     * Required relationship, cannot be null.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "username",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_transaction_user")
    )
    @NotNull
    private User user;

    /**
     * Type of the transaction - TRANSFER, EXPENSE, or INCOME.
     * Required field to determine transaction behavior and validation rules.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 10)
    @NotNull
    private TransactionType transactionType;

    /**
     * Source wallet for the transaction.
     * Required for TRANSFER and EXPENSE transactions.
     * Can be NULL for INCOME transactions or external transfers.
     */
    @ManyToOne(optional = true)
    @JoinColumn(
        name = "wallet_from_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_transaction_wallet_from")
    )
    private Wallet walletFrom;

    /**
     * Destination wallet for the transaction.
     * Required for TRANSFER and INCOME transactions.
     * Can be NULL for EXPENSE transactions or external transfers.
     */
    @ManyToOne(optional = true)
    @JoinColumn(
        name = "wallet_to_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_transaction_wallet_to")
    )
    private Wallet walletTo;

    /**
     * Base transaction amount in the source wallet's currency.
     * Required field with precision validation (17,2).
     * Must be positive for all transaction types.
     */
    @Column(name = "amount", nullable = false, precision = 17, scale = 2)
    @NotNull
    @Digits(integer = 15, fraction = 2)
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Exchange rate used to convert amount to destination wallet currency.
     * Defaults to 1.0 for same-currency transactions.
     * Required field with precision validation (17,6).
     */
    @Column(name = "exchange_rate", nullable = false, precision = 17, scale = 6)
    @NotNull
    @Digits(integer = 11, fraction = 6)
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    /**
     * Calculated amount in the destination wallet's currency.
     * Computed as: amount * exchangeRate
     * This is a transient field calculated on-the-fly to ensure data consistency.
     */
    @Transient
    private BigDecimal walletToAmount;

    /**
     * Getter for the calculated wallet to amount.
     * This method calculates the value on-the-fly based on amount and exchange rate.
     */
    public BigDecimal getWalletToAmount() {
        if (amount != null && exchangeRate != null) {
            this.walletToAmount = amount.multiply(exchangeRate);
        }
        return this.walletToAmount;
    }

    /**
     * Optional note providing additional details about the transaction.
     * Optional field with length validation.
     */
    @Column(name = "note", length = 1000)
    @Size(max = 1000)
    private String note;

    /**
     * Effective date when the transaction takes effect.
     * Required field to determine when the transaction should be processed.
     */
    @Column(name = "effective_date", nullable = false)
    @NotNull
    private LocalDate effectiveDate;

    /**
     * Category for the transaction.
     * Applicable for EXPENSE and INCOME transactions (not required for TRANSFER).
     * Optional field to allow for better transaction organization.
     */
    @ManyToOne(optional = true)
    @JoinColumn(
        name = "category_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_transaction_category")
    )
    private Category category;

}
