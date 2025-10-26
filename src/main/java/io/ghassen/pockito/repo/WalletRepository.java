package io.ghassen.pockito.repo;

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
import java.util.UUID;

/**
 * Repository interface for Wallet entity operations.
 * 
 * Provides standard CRUD operations and custom query methods for wallet management.
 * Extends JpaRepository to inherit common database operations.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Find all wallets belonging to a specific user.
     * 
     * @param username the username of the user
     * @return list of wallets owned by the user, ordered by order position
     */
    List<Wallet> findByUserUsernameOrderByOrderPositionAsc(String username);

    /**
     * Find all wallets of a specific type belonging to a user.
     * 
     * @param username the username of the user
     * @param type the wallet type to filter by
     * @return list of wallets of the specified type owned by the user
     */
    List<Wallet> findByUserUsernameAndTypeOrderByOrderPositionAsc(String username, WalletType type);

    /**
     * Find the default wallet for a specific user.
     * 
     * @param username the username of the user
     * @return optional containing the default wallet if it exists
     */
    Optional<Wallet> findByUserUsernameAndIsDefaultTrue(String username);

    /**
     * Check if a user has a wallet with a specific name.
     * 
     * @param username the username of the user
     * @param name the wallet name to check
     * @return true if a wallet with the given name exists for the user
     */
    boolean existsByUserUsernameAndName(String username, String name);

    /**
     * Find wallet by user and name.
     * 
     * @param username the username of the user
     * @param name the wallet name
     * @return optional containing the wallet if it exists
     */
    Optional<Wallet> findByUserUsernameAndName(String username, String name);

    /**
     * Count wallets belonging to a specific user.
     * 
     * @param username the username of the user
     * @return the number of wallets owned by the user
     */
    long countByUserUsername(String username);

    /**
     * Find wallets by currency for a specific user.
     * 
     * @param username the username of the user
     * @param currency the currency code to filter by
     * @return list of wallets with the specified currency
     */
    List<Wallet> findByUserUsernameAndCurrencyOrderByOrderPositionAsc(String username, String currency);

    /**
     * Find the highest order position for wallets belonging to a user.
     * 
     * @param username the username of the user
     * @return the highest order position, or 0 if no wallets exist
     */
    @Query("SELECT COALESCE(MAX(w.orderPosition), 0) FROM Wallet w WHERE w.user.username = :username")
    int findMaxOrderPositionByUserUsername(@Param("username") String username);

    /**
     * Find wallets with order position greater than or equal to a given value for a user.
     * Used for reordering operations.
     * 
     * @param username the username of the user
     * @param orderPosition the minimum order position
     * @return list of wallets that need to be reordered
     */
    List<Wallet> findByUserUsernameAndOrderPositionGreaterThanEqualOrderByOrderPositionAsc(
        String username, int orderPosition);

    /**
     * Set a specific wallet as default and set all other wallets for the user as non-default.
     * This method ensures only one wallet per user can be default.
     * 
     * @param username the username of the user
     * @param walletId the wallet ID to set as default
     */
    @Query("UPDATE Wallet w SET w.isDefault = CASE WHEN w.id = :walletId THEN true ELSE false END " +
           "WHERE w.user.username = :username")
    @Modifying(clearAutomatically = true)
    @Transactional
    void setDefaultWalletForUser(@Param("username") String username, @Param("walletId") UUID walletId);
}
