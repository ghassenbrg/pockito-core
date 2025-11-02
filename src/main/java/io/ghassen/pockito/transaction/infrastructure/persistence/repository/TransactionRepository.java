package io.ghassen.pockito.transaction.infrastructure.persistence.repository;

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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByUserUsernameOrderByEffectiveDateDesc(String username);

    List<Transaction> findByWalletFromIdOrderByEffectiveDateDesc(String walletId);

    List<Transaction> findByWalletToIdOrderByEffectiveDateDesc(String walletId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.walletFrom.id = :walletId")
    BigDecimal calculateTotalOutgoingAmount(@Param("walletId") String walletId);

    @Query("SELECT COALESCE(SUM(t.amount * t.exchangeRate), 0) FROM Transaction t WHERE t.walletTo.id = :walletId")
    BigDecimal calculateTotalIncomingAmount(@Param("walletId") String walletId);

    @Query("SELECT w.initialBalance + " +
           "COALESCE((SELECT SUM(t.amount * t.exchangeRate) FROM Transaction t WHERE t.walletTo.id = :walletId), 0) - " +
           "COALESCE((SELECT SUM(t.amount) FROM Transaction t WHERE t.walletFrom.id = :walletId), 0) " +
           "FROM Wallet w WHERE w.id = :walletId")
    BigDecimal calculateCurrentBalance(@Param("walletId") String walletId);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "ORDER BY t.effectiveDate DESC")
    List<Transaction> findByUserAndWallet(@Param("username") String username, @Param("walletId") String walletId);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.walletFrom.id = :walletId OR t.walletTo.id = :walletId")
    boolean hasTransactions(@Param("walletId") String walletId);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (:walletId IS NULL OR t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "AND (:startDate IS NULL OR t.effectiveDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.effectiveDate <= :endDate) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserWithFilters(
            @Param("username") String username,
            @Param("walletId") String walletId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND (t.walletFrom.id = :walletId OR t.walletTo.id = :walletId) " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndWalletWithPagination(
            @Param("username") String username,
            @Param("walletId") String walletId,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND t.effectiveDate >= :startDate AND t.effectiveDate <= :endDate " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndDateRange(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.username = :username " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.effectiveDate DESC, t.createdAt DESC")
    Page<Transaction> findByUserAndTransactionType(
            @Param("username") String username,
            @Param("transactionType") TransactionType transactionType,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.walletFrom.id = :walletId OR t.walletTo.id = :walletId")
    List<Transaction> findAllByWalletId(@Param("walletId") String walletId);

    @Query("SELECT t FROM Transaction t WHERE t.subscription.id = :subscriptionId")
    List<Transaction> findBySubscriptionId(@Param("subscriptionId") String subscriptionId);
}


