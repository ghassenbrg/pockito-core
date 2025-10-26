package io.ghassen.pockito.web.types.dto;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Internal DTO for Transaction entity.
 * 
 * Used for transferring transaction data between the web layer and service layer.
 * Contains all necessary fields for internal processing.
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
     */
    private String username;

    /**
     * Type of the transaction - TRANSFER, EXPENSE, or INCOME.
     */
    private TransactionType transactionType;

    /**
     * Source wallet ID for the transaction.
     */
    private UUID walletFromId;

    /**
     * Name of the source wallet.
     */
    private String walletFromName;

    /**
     * Destination wallet ID for the transaction.
     */
    private UUID walletToId;

    /**
     * Name of the destination wallet.
     */
    private String walletToName;

    /**
     * Base transaction amount in the source wallet's currency.

    /**
     * Currency of the source wallet.
     */
    private CurrencyCode walletFromCurrency;

    /**
     * Currency of the destination wallet.
     */
    private CurrencyCode walletToCurrency;

    /**
     * Currency of the destination wallet.
     */
    private BigDecimal amount;

    /**
     * Exchange rate used to convert amount to destination wallet currency.
     */
    private BigDecimal exchangeRate;

    /**
     * Amount in the destination wallet's currency.
     * Computed as: amount * exchangeRate
     */
    private BigDecimal walletToAmount;

    /**
     * Category ID for the transaction.
     */
    private UUID categoryId;

    /**
     * Name of the category.
     */
    private String categoryName;

    /**
     * Icon URL of the category.
     */
    private String iconUrl;

    /**
     * Description or note for the transaction.
     */
    private String note;

    /**
     * Effective date of the transaction.
     */
    private LocalDate effectiveDate;

    /**
     * When the transaction was created.
     */
    private Instant createdAt;

    /**
     * When the transaction was last updated.
     */
    private Instant updatedAt;
}
