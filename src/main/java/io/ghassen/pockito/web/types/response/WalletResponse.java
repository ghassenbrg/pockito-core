package io.ghassen.pockito.web.types.response;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO for Wallet entity.
 * 
 * Used for API responses, providing complete wallet information
 * including calculated fields like current balance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    /**
     * Wallet unique identifier.
     */
    private String id;

    /**
     * Username of the wallet owner.
     */
    private String username;

    /**
     * Wallet name.
     */
    private String name;

    /**
     * Description of the wallet.
     */
    private String description;

    /**
     * Color of the wallet.
     */
    private String color;

    /**
     * Initial balance of the wallet.
     */
    private BigDecimal initialBalance;

    /**
     * Current balance of the wallet.
     */
    private BigDecimal balance;

    /**
     * Currency code for the wallet.
     */
    private CurrencyCode currency;

    /**
     * URL to the wallet's icon/image.
     */
    private String iconUrl;

    /**
     * Goal amount for the wallet (e.g., savings target).
     */
    private BigDecimal goalAmount;

    /**
     * Type/category of the wallet.
     */
    private WalletType type;

    /**
     * Whether this wallet is the user's default wallet.
     */
    private Boolean isDefault;

    /**
     * Order position for wallet sorting.
     */
    private Integer orderPosition;

    /**
     * When the wallet was created.
     */
    private Instant createdAt;

    /**
     * When the wallet was last updated.
     */
    private Instant updatedAt;

    /**
     * Whether the wallet is active (not archived).
     */
    private Boolean active;
}
