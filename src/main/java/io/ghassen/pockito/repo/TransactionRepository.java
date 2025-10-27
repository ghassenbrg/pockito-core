package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * Find transactions for a specific user with pagination and filtering.
     * 
     * @param username the username to filter by
     * @param walletId optional wallet ID to filter by (can be null)
     * @param startDate optional start date for date range filtering (can be null)
     * @param endDate optional end date for date range filtering (can be null)
     * @param transactionType optional transaction type to filter by (can be null)
     * @param pageable pagination information
     * @return page of transactions matching the criteria
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (:walletId IS NULL OR t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "AND (:startDate IS NULL OR t.effectiveDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.effectiveDate <= :endDate) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserWithFilters(
            @Param("username") String username,
            @Param("walletId") UUID walletId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    /**
     * Find transactions for a specific user and wallet with pagination.
     * 
     * @param username the username to filter by
     * @param walletId the wallet ID to filter by
     * @param pageable pagination information
     * @return page of transactions involving the specified wallet
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndWalletWithPagination(
            @Param("username") String username,
            @Param("walletId") UUID walletId,
            Pageable pageable);

    /**
     * Find transactions for a specific user with date range filtering and pagination.
     * 
     * @param username the username to filter by
     * @param startDate the start date for date range filtering
     * @param endDate the end date for date range filtering
     * @param pageable pagination information
     * @return page of transactions within the date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND t.effectiveDate >= :startDate AND t.effectiveDate <= :endDate " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndDateRange(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find transactions for a specific user by transaction type with pagination.
     * 
     * @param username the username to filter by
     * @param transactionType the transaction type to filter by
     * @param pageable pagination information
     * @return page of transactions of the specified type
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndTransactionType(
            @Param("username") String username,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    /**
     * Find all transactions for a specific wallet (either as source or destination).
     * Used for handling transactions when a wallet is deleted.
     * 
     * @param walletId the wallet ID to filter by
     * @return list of transactions involving the specified wallet
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletFrom.id = :walletId OR t.walletTo.id = :walletId")
    List<Transaction> findAllByWalletId(@Param("walletId") UUID walletId);
}
