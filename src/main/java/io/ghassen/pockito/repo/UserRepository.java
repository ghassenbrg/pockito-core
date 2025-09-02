package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * Provides basic CRUD operations and custom query methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find a user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);
}
