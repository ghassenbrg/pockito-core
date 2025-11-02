package io.ghassen.pockito.web.types.response;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for Transaction entity.
 * 
 * Used for API responses, providing complete transaction information
 * including calculated fields like converted amount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    /**
     * Transaction unique identifier.
     */
    private String id;

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
    private String walletFromId;

    /**
     * Name of the source wallet.
     */
    private String walletFromName;

    /**
     * Destination wallet ID for the transaction.
     */
    private String walletToId;

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
    private String categoryId;

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
     * Subscription ID that generated this transaction.
     */
    private String subscriptionId;

    /**
     * Name of the subscription that generated this transaction.
     */
    private String subscriptionName;

    /**
     * When the transaction was created.
     */
    private Instant createdAt;

    /**
     * When the transaction was last updated.
     */
    private Instant updatedAt;
}
