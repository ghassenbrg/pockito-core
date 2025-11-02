package io.ghassen.pockito.subscription.web.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import io.ghassen.pockito.domain.validation.WalletId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySubscriptionRequest {

    @WalletId
    private String walletId;

    @Digits(integer = 11, fraction = 6, message = "Exchange rate must have at most 11 digits and 6 decimal places")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    private BigDecimal exchangeRate;

    private Boolean skip;
}


