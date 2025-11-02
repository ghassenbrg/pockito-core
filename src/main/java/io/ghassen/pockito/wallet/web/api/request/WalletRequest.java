package io.ghassen.pockito.wallet.web.api.request;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.WalletType;
import io.ghassen.pockito.domain.validation.ValidationGroups;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

    @NotBlank(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @NotNull(message = "Initial balance is required")
    @Digits(integer = 15, fraction = 2, message = "Initial balance must have at most 15 digits and 2 decimal places")
    private BigDecimal initialBalance;

    @NotNull(message = "Currency is required", groups = ValidationGroups.Create.class)
    private CurrencyCode currency;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Goal amount must be non-negative")
    @Digits(integer = 15, fraction = 2, message = "Goal amount must have at most 15 digits and 2 decimal places")
    private BigDecimal goalAmount;

    @NotNull(message = "Wallet type is required")
    private WalletType type;

    @NotNull(message = "Default flag is required")
    private Boolean isDefault;
}


