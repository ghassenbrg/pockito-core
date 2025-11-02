package io.ghassen.pockito.web.types.request;

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

/**
 * Request DTO for creating and updating wallets.
 * 
 * Used as API request payload for wallet operations.
 * Excludes fields that are automatically managed by the system (id, username, balance).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

    /**
     * Wallet name - must be unique per user.
     */
    @NotBlank(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    private String name;

    /**
     * Description of the wallet.
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Color of the wallet.
     */
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    /**
     * Initial balance of the wallet.
     * Can be negative to represent debt or overdraft.
     */
    @NotNull(message = "Initial balance is required")
    @Digits(integer = 15, fraction = 2, message = "Initial balance must have at most 15 digits and 2 decimal places")
    private BigDecimal initialBalance;

    /**
     * Currency code for the wallet.
     * Required only when creating a new wallet, not when updating.
     */
    @NotNull(message = "Currency is required", groups = ValidationGroups.Create.class)
    private CurrencyCode currency;

    /**
     * URL to the wallet's icon/image.
     */
    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    /**
     * Goal amount for the wallet (e.g., savings target).
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Goal amount must be non-negative")
    @Digits(integer = 15, fraction = 2, message = "Goal amount must have at most 15 digits and 2 decimal places")
    private BigDecimal goalAmount;

    /**
     * Type/category of the wallet.
     */
    @NotNull(message = "Wallet type is required")
    private WalletType type;

    /**
     * Whether this wallet is the user's default wallet.
     */
    @NotNull(message = "Default flag is required")
    private Boolean isDefault;
}
