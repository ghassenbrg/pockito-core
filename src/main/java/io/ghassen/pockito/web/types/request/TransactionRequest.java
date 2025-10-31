package io.ghassen.pockito.web.types.request;

import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.web.validation.CategoryId;
import io.ghassen.pockito.web.validation.WalletId;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating and updating transactions.
 * 
 * Used as API request payload for transaction operations.
 * Excludes fields that are automatically managed by the system (id, username, calculatedAmount).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

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
    @WalletId
    private String walletFromId;

    /**
     * Destination wallet ID for the transaction.
     * Required for TRANSFER and INCOME transactions.
     * Can be NULL for EXPENSE transactions or external transfers.
     */
    @WalletId
    private String walletToId;

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
     * Category ID for the transaction.
     * Required for EXPENSE and INCOME transactions.
     * Not applicable for TRANSFER transactions.
     */
    @CategoryId
    private String categoryId;

    /**
     * Description or note for the transaction.
     */
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    /**
     * Effective date of the transaction.
     * Defaults to current date if not provided.
     */
    private LocalDate effectiveDate;
}
