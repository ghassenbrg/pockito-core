package io.ghassen.pockito.web.types.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for lists of wallets.
 * 
 * Used to wrap wallet lists in API responses instead of returning List directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletListResponse {

    /**
     * List of wallets.
     */
    private List<WalletResponse> wallets;

    /**
     * Total count of wallets (useful for pagination).
     */
    private Long totalCount;
}
