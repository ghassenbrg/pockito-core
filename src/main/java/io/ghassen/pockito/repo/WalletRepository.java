package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Wallet;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
  
  // Order by display_order instead of created_at
  List<Wallet> findByUserIdOrderByDisplayOrder(String userId);
  
  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.isDefault = true")
  Optional<Wallet> findByUserIdAndIsDefaultTrue(@Param("userId") String userId);
  
  boolean existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(String userId, String name);
  
  Optional<Wallet> findByIdAndUserId(UUID id, String userId);
  
  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.archivedAt IS NULL ORDER BY w.displayOrder ASC")
  List<Wallet> findActiveByUserId(@Param("userId") String userId);
  
  @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.userId = :userId AND LOWER(w.name) = LOWER(:name) AND w.archivedAt IS NULL")
  boolean existsActiveByNameIgnoreCase(@Param("userId") String userId, @Param("name") String name);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from Wallet w where w.userId = :userId and w.isDefault = true")
  Optional<Wallet> findDefaultForUpdate(@Param("userId") String userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
         update Wallet w
            set w.isDefault = case when w.id = :id then true else false end
          where w.userId = :userId
         """)
  int setDefaultForUser(@Param("userId") String userId, @Param("id") UUID id);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.name LIKE %:searchTerm% ORDER BY w.displayOrder ASC")
  List<Wallet> searchByUserIdAndTerm(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.name LIKE %:searchTerm% AND w.archivedAt IS NULL ORDER BY w.displayOrder ASC")
  List<Wallet> searchActiveByUserIdAndTerm(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.type = :type ORDER BY w.displayOrder ASC")
  List<Wallet> findByUserIdAndType(@Param("userId") String userId, @Param("type") Wallet.WalletType type);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.type = :type AND w.archivedAt IS NULL ORDER BY w.displayOrder ASC")
  List<Wallet> findActiveByUserIdAndType(@Param("userId") String userId, @Param("type") Wallet.WalletType type);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.currencyCode = :currencyCode ORDER BY w.displayOrder ASC")
  List<Wallet> findByUserIdAndCurrencyCode(@Param("userId") String userId, @Param("currencyCode") String currencyCode);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.currencyCode = :currencyCode AND w.archivedAt IS NULL ORDER BY w.displayOrder ASC")
  List<Wallet> findActiveByUserIdAndCurrencyCode(@Param("userId") String userId, @Param("currencyCode") String currencyCode);

  // Get max display order for a user
  @Query("SELECT COALESCE(MAX(w.displayOrder), 0) FROM Wallet w WHERE w.userId = :userId AND w.archivedAt IS NULL")
  Integer findMaxDisplayOrderByUserId(@Param("userId") String userId);

  // Update display order for a specific wallet
  @Modifying
  @Query("UPDATE Wallet w SET w.displayOrder = :newOrder WHERE w.id = :walletId AND w.userId = :userId")
  int updateDisplayOrder(@Param("walletId") UUID walletId, @Param("userId") String userId, @Param("newOrder") Integer newOrder);

  // Reorder wallets between two positions
  @Modifying
  @Query("""
         UPDATE Wallet w 
         SET w.displayOrder = CASE 
           WHEN w.displayOrder >= :fromOrder AND w.displayOrder < :toOrder THEN w.displayOrder + 1
           WHEN w.displayOrder = :toOrder THEN :fromOrder
           ELSE w.displayOrder
         END
         WHERE w.userId = :userId AND w.archivedAt IS NULL
         """)
  int reorderWallets(@Param("userId") String userId, @Param("fromOrder") Integer fromOrder, @Param("toOrder") Integer toOrder);
}
