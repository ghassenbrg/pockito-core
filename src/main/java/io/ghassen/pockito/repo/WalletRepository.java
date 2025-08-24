package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
  
  List<Wallet> findByUserIdOrderByCreatedAtDesc(String userId);
  
  Optional<Wallet> findByUserIdAndIsDefaultTrue(String userId);
  
  boolean existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(String userId, String name);
  
  Optional<Wallet> findByIdAndUserId(UUID id, String userId);
  
  @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.archivedAt IS NULL ORDER BY w.createdAt DESC")
  List<Wallet> findActiveByUserId(@Param("userId") String userId);
  
  @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.userId = :userId AND LOWER(w.name) = LOWER(:name) AND w.archivedAt IS NULL")
  boolean existsActiveByNameIgnoreCase(@Param("userId") String userId, @Param("name") String name);
}
