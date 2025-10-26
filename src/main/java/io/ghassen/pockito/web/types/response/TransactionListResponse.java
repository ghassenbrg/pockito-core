package io.ghassen.pockito.web.types.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for lists of transactions.
 * 
 * Used to wrap transaction lists in API responses instead of returning List directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    /**
     * List of transactions.
     */
    private List<TransactionResponse> transactions;

    /**
     * Total count of transactions (useful for pagination).
     */
    private Long totalCount;
}
