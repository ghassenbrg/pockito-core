package io.ghassen.pockito.wallet.infrastructure.persistence.repository;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    List<Wallet> findByUserUsernameOrderByOrderPositionAsc(String username);

    List<Wallet> findByUserUsernameAndTypeOrderByOrderPositionAsc(String username, WalletType type);

    Optional<Wallet> findByUserUsernameAndIsDefaultTrue(String username);

    boolean existsByUserUsernameAndName(String username, String name);

    Optional<Wallet> findByUserUsernameAndName(String username, String name);

    long countByUserUsername(String username);

    List<Wallet> findByUserUsernameAndCurrencyOrderByOrderPositionAsc(String username, String currency);

    @Query("SELECT COALESCE(MAX(w.orderPosition), 0) FROM Wallet w WHERE w.user.username = :username")
    int findMaxOrderPositionByUserUsername(@Param("username") String username);

    List<Wallet> findByUserUsernameAndOrderPositionGreaterThanEqualOrderByOrderPositionAsc(
        String username, int orderPosition);

    @Query("UPDATE Wallet w SET w.isDefault = CASE WHEN w.id = :walletId THEN true ELSE false END " +
           "WHERE w.user.username = :username")
    @Modifying(clearAutomatically = true)
    @Transactional
    void setDefaultWalletForUser(@Param("username") String username, @Param("walletId") String walletId);

    @Query("UPDATE Wallet w SET w.isDefault = CASE " +
           "WHEN w.orderPosition = (SELECT MIN(w2.orderPosition) FROM Wallet w2 " +
           "WHERE w2.user.username = :username AND w2.id != :excludeWalletId) THEN true " +
           "ELSE false END " +
           "WHERE w.user.username = :username")
    @Modifying(clearAutomatically = true)
    @Transactional
    void setFirstRemainingWalletAsDefault(@Param("username") String username, @Param("excludeWalletId") String excludeWalletId);
}


