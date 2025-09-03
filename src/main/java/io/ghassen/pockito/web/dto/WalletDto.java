package io.ghassen.pockito.web.dto;

import io.ghassen.pockito.domain.CurrencyCode;
import io.ghassen.pockito.domain.WalletType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object for Wallet entity.
 * 
 * Used for transferring wallet data between the web layer and service layer.
 * Includes validation annotations to ensure data integrity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {

    /**
     * Wallet unique identifier.
     */
    private UUID id;

    /**
     * Username of the wallet owner.
     * This field is automatically set from the authenticated user and cannot be updated.
     */
    private String username;

    /**
     * Wallet name - must be unique per user.
     */
    @NotBlank(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    private String name;

    /**
     * Initial balance of the wallet.
     * Can be negative to represent debt or overdraft.
     */
    @NotNull(message = "Initial balance is required")
    @Digits(integer = 15, fraction = 2, message = "Initial balance must have at most 15 digits and 2 decimal places")
    private BigDecimal initialBalance;

    /**
     * Current balance of the wallet.
     * Initially set to initial balance, later calculated based on transactions.
     */
    private BigDecimal balance;

    /**
     * Currency code for the wallet.
     */
    @NotNull(message = "Currency is required")
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

    /**
     * Display order position for the wallet.
     */
    @DecimalMin(value = "0", inclusive = true, message = "Order position must be non-negative")
    private Integer orderPosition;

    /**
     * Description of the wallet.
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Hex color code for the wallet (e.g., #A1B2C3).
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #A1B2C3)")
    @Size(min = 7, max = 7, message = "Color must be exactly 7 characters including the # symbol")
    private String color;

    /**
     * Whether the wallet is active (not archived).
     * Derived from archivedAt field - true if archivedAt is null, false otherwise.
     */
    private Boolean active;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    private Instant updatedAt;

    /**
     * Username of who created the wallet.
     */
    private String createdBy;

    /**
     * Username of who last updated the wallet.
     */
    private String updatedBy;
}
