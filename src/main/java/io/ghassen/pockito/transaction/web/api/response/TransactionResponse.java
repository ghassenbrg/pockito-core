package io.ghassen.pockito.transaction.web.api.response;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String id;

    private String username;

    private TransactionType transactionType;

    private String walletFromId;

    private String walletFromName;

    private String walletToId;

    private String walletToName;

    private CurrencyCode walletFromCurrency;

    private CurrencyCode walletToCurrency;

    private BigDecimal amount;

    private BigDecimal exchangeRate;

    private BigDecimal walletToAmount;

    private String categoryId;

    private String categoryName;

    private String iconUrl;

    private String note;

    private LocalDate effectiveDate;

    private String subscriptionId;

    private String subscriptionName;

    private Instant createdAt;

    private Instant updatedAt;
}


