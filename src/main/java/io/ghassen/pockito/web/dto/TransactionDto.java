package io.ghassen.pockito.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import io.ghassen.pockito.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Transaction entity.
 * 
 * Used for transferring transaction data between the web layer and service
 * layer.
 * Includes validation annotations to ensure data integrity and business rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    /**
     * Transaction unique identifier.
     */
    private UUID id;

    /**
     * Username of the transaction owner.
     * This field is automatically set from the authenticated user and cannot be
     * updated.
     */
    private String username;

    /**
     * Type of the transaction - TRANSFER, EXPENSE, or INCOME.
     * Required field to determine transaction behavior and validation rules.
     */
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    /**
     * Source wallet ID for the transaction.
     * Required for TRANSFER and EXPENSE transactions.
     * Can be NULL for INCOME transactions or external transfers.
     */
    private UUID walletFromId;

    /**
     * Destination wallet ID for the transaction.
     * Required for TRANSFER and INCOME transactions.
     * Can be NULL for EXPENSE transactions or external transfers.
     */
    private UUID walletToId;

    /**
     * Base transaction amount in the source wallet's currency.
     * Required field with precision validation (17,2).
     * Must be positive for all transaction types.
     */
    @NotNull(message = "Amount is required")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 digits and 2 decimal places")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Exchange rate used to convert amount to destination wallet currency.
     * Defaults to 1.0 for same-currency transactions.
     * Required field with precision validation (17,6).
     */
    @NotNull(message = "Exchange rate is required")
    @Digits(integer = 11, fraction = 6, message = "Exchange rate must have at most 11 digits and 6 decimal places")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    /**
     * Calculated amount in the destination wallet's currency.
     * Computed as: amount * exchangeRate
     * This is a read-only field calculated on-the-fly.
     */
    private BigDecimal walletToAmount;

    /**
     * Optional note providing additional details about the transaction.
     * Optional field with length validation.
     */
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    /**
     * Effective date when the transaction takes effect.
     * Required field to determine when the transaction should be processed.
     */
    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    /**
     * Category ID for the transaction.
     * Applicable for EXPENSE and INCOME transactions (not required for TRANSFER).
     * Optional field to allow for better transaction organization.
     */
    private UUID categoryId;

    /**
     * Name of the source wallet (for display purposes).
     * Populated automatically from walletFrom entity.
     */
    private String walletFromName;

    /**
     * Name of the destination wallet (for display purposes).
     * Populated automatically from walletTo entity.
     */
    private String walletToName;

    /**
     * Name of the category (for display purposes).
     * Populated automatically from category entity.
     */
    private String categoryName;

    /**
     * Currency code of the source wallet (for display purposes).
     * Populated automatically from walletFrom entity.
     */
    private String walletFromCurrency;

    /**
     * Currency code of the destination wallet (for display purposes).
     * Populated automatically from walletTo entity.
     */
    private String walletToCurrency;

    /**
     * Icon URL of the category (for display purposes).
     * Populated automatically from category entity.
     * Null if no category is set.
     */
    private String iconUrl;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    private Instant updatedAt;
}
