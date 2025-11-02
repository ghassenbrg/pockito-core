package io.ghassen.pockito.web.types.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.ghassen.pockito.domain.validation.WalletId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for reordering wallets.
 * Contains the list of wallet IDs in the desired order.
 */
public class ReorderWalletsRequest {

    @JsonProperty("walletIds")
    @NotNull(message = "Wallet IDs list cannot be null")
    @NotEmpty(message = "Wallet IDs list cannot be empty")
    @Valid
    private List<@WalletId String> walletIds;

    /**
     * Default constructor.
     */
    public ReorderWalletsRequest() {
    }

    /**
     * Constructor with wallet IDs.
     *
     * @param walletIds the list of wallet IDs in the desired order
     */
    public ReorderWalletsRequest(List<String> walletIds) {
        this.walletIds = walletIds;
    }

    /**
     * Get the list of wallet IDs.
     *
     * @return the list of wallet IDs
     */
    public List<String> getWalletIds() {
        return walletIds;
    }

    /**
     * Set the list of wallet IDs.
     *
     * @param walletIds the list of wallet IDs
     */
    public void setWalletIds(List<String> walletIds) {
        this.walletIds = walletIds;
    }

    @Override
    public String toString() {
        return "ReorderWalletsRequest{" +
                "walletIds=" + walletIds +
                '}';
    }
}
