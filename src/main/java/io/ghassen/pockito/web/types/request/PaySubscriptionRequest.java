package io.ghassen.pockito.web.types.request;

import io.ghassen.pockito.web.validation.WalletId;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for paying a subscription.
 * 
 * Used as API request payload for subscription payment operations.
 * Contains the wallet ID to charge the subscription from and optional exchange rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySubscriptionRequest {

    /**
     * Wallet ID to charge the subscription from.
     * Optional - if not provided, uses the subscription's default wallet.
     */
    @WalletId
    private String walletId;

    /**
     * Exchange rate used to convert subscription amount to wallet currency.
     * Optional - only used if subscription currency differs from wallet currency.
     * If currencies match, this value is ignored and exchange rate defaults to 1.0.
     */
    @Digits(integer = 11, fraction = 6, message = "Exchange rate must have at most 11 digits and 6 decimal places")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    private BigDecimal exchangeRate;
}

