package io.ghassen.pockito.wallet.web.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.ghassen.pockito.domain.validation.WalletId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ReorderWalletsRequest {

    @JsonProperty("walletIds")
    @NotNull(message = "Wallet IDs list cannot be null")
    @NotEmpty(message = "Wallet IDs list cannot be empty")
    @Valid
    private List<@WalletId String> walletIds;

    public ReorderWalletsRequest() {
    }

    public ReorderWalletsRequest(List<String> walletIds) {
        this.walletIds = walletIds;
    }

    public List<String> getWalletIds() {
        return walletIds;
    }

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


