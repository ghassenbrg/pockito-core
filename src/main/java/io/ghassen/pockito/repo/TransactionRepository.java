package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Transaction entity.
 * 
 * Provides data access methods for transaction operations including
 * balance calculations and transaction history queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find all transactions for a specific user.
     * 
     * @param username the username to filter by
     * @return list of transactions for the user
     */
    List<Transaction> findByUserUsernameOrderByEffectiveDateDesc(String username);

    /**
     * Find all transactions where the wallet is the source (walletFrom).
     * 
     * @param walletId the wallet ID to filter by
     * @return list of transactions where this wallet is the source
     */
    List<Transaction> findByWalletFromIdOrderByEffectiveDateDesc(UUID walletId);

    /**
     * Find all transactions where the wallet is the destination (walletTo).
     * 
     * @param walletId the wallet ID to filter by
     * @return list of transactions where this wallet is the destination
     */
    List<Transaction> findByWalletToIdOrderByEffectiveDateDesc(UUID walletId);

    /**
     * Calculate the total amount of money that went OUT of a wallet (when wallet is source).
     * This includes EXPENSE transactions and TRANSFER transactions where this wallet is the source.
     * 
     * @param walletId the wallet ID to calculate outgoing amount for
     * @return the total amount that went out of the wallet
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.walletFrom.id = :walletId")
    BigDecimal calculateTotalOutgoingAmount(@Param("walletId") UUID walletId);

    /**
     * Calculate the total amount of money that came INTO a wallet (when wallet is destination).
     * This includes INCOME transactions and TRANSFER transactions where this wallet is the destination.
     * 
     * @param walletId the wallet ID to calculate incoming amount for
     * @return the total amount that came into the wallet
     */
    @Query("SELECT COALESCE(SUM(t.amount * t.exchangeRate), 0) FROM Transaction t WHERE t.walletTo.id = :walletId")
    BigDecimal calculateTotalIncomingAmount(@Param("walletId") UUID walletId);

    /**
     * Calculate the current balance of a wallet based on transactions.
     * Formula: initialBalance + incomingAmount - outgoingAmount
     * 
     * @param walletId the wallet ID to calculate balance for
     * @return the current balance of the wallet
     */
    @Query("SELECT w.initialBalance + " +
           "COALESCE((SELECT SUM(t.amount * t.exchangeRate) FROM Transaction t WHERE t.walletTo.id = :walletId), 0) - " +
           "COALESCE((SELECT SUM(t.amount) FROM Transaction t WHERE t.walletFrom.id = :walletId), 0) " +
           "FROM Wallet w WHERE w.id = :walletId")
    BigDecimal calculateCurrentBalance(@Param("walletId") UUID walletId);

    /**
     * Find all transactions for a specific user and wallet (either as source or destination).
     * 
     * @param username the username to filter by
     * @param walletId the wallet ID to filter by
     * @return list of transactions involving the specified wallet
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "ORDER BY t.effectiveDate DESC")
    List<Transaction> findByUserAndWallet(@Param("username") String username, @Param("walletId") UUID walletId);

    /**
     * Check if a wallet has any transactions.
     * 
     * @param walletId the wallet ID to check
     * @return true if the wallet has transactions, false otherwise
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.walletFrom.id = :walletId OR t.walletTo.id = :walletId")
    boolean hasTransactions(@Param("walletId") UUID walletId);
}
