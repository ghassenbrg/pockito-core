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
  
  List<Wallet> findByUserIdOrderByCreatedAtDesc(String userId);
  
  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.isDefault = true")
  Optional<Wallet> findByUserIdAndIsDefaultTrue(@Param("userId") String userId);
  
  boolean existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(String userId, String name);
  
  Optional<Wallet> findByIdAndUserId(UUID id, String userId);
  
  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.archivedAt IS NULL ORDER BY w.createdAt DESC")
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

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.name LIKE %:searchTerm% ORDER BY w.createdAt DESC")
  List<Wallet> searchByUserIdAndTerm(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.name LIKE %:searchTerm% AND w.archivedAt IS NULL ORDER BY w.createdAt DESC")
  List<Wallet> searchActiveByUserIdAndTerm(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.type = :type ORDER BY w.createdAt DESC")
  List<Wallet> findByUserIdAndType(@Param("userId") String userId, @Param("type") Wallet.WalletType type);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.type = :type AND w.archivedAt IS NULL ORDER BY w.createdAt DESC")
  List<Wallet> findActiveByUserIdAndType(@Param("userId") String userId, @Param("type") Wallet.WalletType type);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.currencyCode = :currencyCode ORDER BY w.createdAt DESC")
  List<Wallet> findByUserIdAndCurrencyCode(@Param("userId") String userId, @Param("currencyCode") String currencyCode);

  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.currencyCode = :currencyCode AND w.archivedAt IS NULL ORDER BY w.createdAt DESC")
  List<Wallet> findActiveByUserIdAndCurrencyCode(@Param("userId") String userId, @Param("currencyCode") String currencyCode);
}
