package io.ghassen.pockito.wallet.web.api.response;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private String id;

    private String username;

    private String name;

    private String description;

    private String color;

    private BigDecimal initialBalance;

    private BigDecimal balance;

    private CurrencyCode currency;

    private String iconUrl;

    private BigDecimal goalAmount;

    private WalletType type;

    private Boolean isDefault;

    private Integer orderPosition;

    private Instant createdAt;

    private Instant updatedAt;

    private Boolean active;
}


