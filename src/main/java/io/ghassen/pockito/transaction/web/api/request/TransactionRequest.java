package io.ghassen.pockito.transaction.web.api.request;

import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.domain.validation.CategoryId;
import io.ghassen.pockito.domain.validation.WalletId;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @WalletId
    private String walletFromId;

    @WalletId
    private String walletToId;
    
    @NotNull(message = "Amount is required")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 digits and 2 decimal places")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Exchange rate is required")
    @Digits(integer = 11, fraction = 6, message = "Exchange rate must have at most 11 digits and 6 decimal places")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @CategoryId
    private String categoryId;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    private LocalDate effectiveDate;
}


