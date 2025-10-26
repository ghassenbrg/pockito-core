package io.ghassen.pockito.domain.enums;

/**
 * Enum representing the type of transaction.
 * 
 * Transactions can be categorized into three main types:
 * - TRANSFER: Money movement between wallets (internal or external)
 * - EXPENSE: Money going out of the user's accounts
 * - INCOME: Money coming into the user's accounts
 * 
 * This distinction helps users organize their financial transactions appropriately
 * and enables different business logic for each transaction type.
 */
public enum TransactionType {
    
    /**
     * Transfer transaction - money movement between wallets.
     * Can be internal (between user's own wallets) or external (to/from external wallets).
     * For external transfers, one of the wallet fields (from or to) can be NULL.
     * Categories are not applicable for transfer transactions.
     */
    TRANSFER,
    
    /**
     * Expense transaction - money going out of the user's accounts.
     * Requires a category to be specified for proper organization.
     * Only requires a source wallet (walletFrom).
     */
    EXPENSE,
    
    /**
     * Income transaction - money coming into the user's accounts.
     * Requires a category to be specified for proper organization.
     * Only requires a destination wallet (walletTo).
     */
    INCOME
}
