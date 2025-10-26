package io.ghassen.pockito.web.types.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for reordering wallets.
 * Contains the list of wallet IDs in the desired order.
 */
public class ReorderWalletsRequest {

    @JsonProperty("walletIds")
    @NotNull(message = "Wallet IDs list cannot be null")
    @NotEmpty(message = "Wallet IDs list cannot be empty")
    private List<UUID> walletIds;

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
    public ReorderWalletsRequest(List<UUID> walletIds) {
        this.walletIds = walletIds;
    }

    /**
     * Get the list of wallet IDs.
     *
     * @return the list of wallet IDs
     */
    public List<UUID> getWalletIds() {
        return walletIds;
    }

    /**
     * Set the list of wallet IDs.
     *
     * @param walletIds the list of wallet IDs
     */
    public void setWalletIds(List<UUID> walletIds) {
        this.walletIds = walletIds;
    }

    @Override
    public String toString() {
        return "ReorderWalletsRequest{" +
                "walletIds=" + walletIds +
                '}';
    }
}
