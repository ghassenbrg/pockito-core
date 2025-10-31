package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for user management operations.
 * 
 * Handles the creation and retrieval of users based on Keycloak authentication.
 * Implements the requirement to create users on first backend call authenticated
 * by a Keycloak token.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CategoryRepository categoryRepository;

    /**
     * Get or create a user based on the username from Keycloak token.
     * 
     * This method implements the core requirement:
     * - If username does not exist → create a new User with username
     * - If username exists → load existing user (no duplication)
     * 
     * @param username the username extracted from Keycloak token (preferred_username claim)
     * @return the existing or newly created user
     */
    @Transactional
    public User getOrCreateUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            log.debug("User with username '{}' already exists, returning existing user", username);
            return existingUser.get();
        }

        // Create new user
        log.info("Creating new user with username '{}'", username);
        User newUser = User.builder()
                .username(username)
                .systemAction(true) // Mark as system action for audit trail
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Successfully created user with username '{}'", username);
        
        // Duplicate system categories for the new user
        duplicateSystemCategoriesForUser(savedUser);
        
        return savedUser;
    }

    /**
     * Find a user by username.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    /**
     * Check if a user exists by username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    /**
     * Update user's country.
     * 
     * @param username the username of the user to update
     * @param country the new country value
     * @return the updated user, or empty if user not found
     */
    @Transactional
    public Optional<User> updateCountry(String username, String countryCode) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    try {
                        user.setCountry(io.ghassen.pockito.domain.enums.Country.fromCode(countryCode));
                        user.setSystemAction(true); // Mark as system action for audit trail
                        return userRepository.save(user);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid country code '{}' for user '{}': {}", countryCode, username, e.getMessage());
                        return user; // Return user unchanged if country code is invalid
                    }
                });
    }

    /**
     * Update user's default currency.
     * 
     * @param username the username of the user to update
     * @param currencyCode the new currency code
     * @return the updated user, or empty if user not found
     */
    @Transactional
    public Optional<User> updateDefaultCurrency(String username, String currencyCode) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    try {
                        user.setDefaultCurrency(io.ghassen.pockito.domain.enums.CurrencyCode.fromCode(currencyCode));
                        user.setSystemAction(true); // Mark as system action for audit trail
                        return userRepository.save(user);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid currency code '{}' for user '{}': {}", currencyCode, username, e.getMessage());
                        return user; // Return user unchanged if currency code is invalid
                    }
                });
    }


    /**
     * Duplicate all system categories for a new user.
     * This method creates copies of all categories owned by the "system" user
     * and assigns them to the new user, maintaining the hierarchical structure.
     * 
     * @param newUser the user to create categories for
     */
    @Transactional
    public void duplicateSystemCategoriesForUser(User newUser) {
        final String systemUsername = "system";
        log.info("Duplicating system categories for new user: {}", newUser.getUsername());
        
        // Get all system categories in hierarchical order (parents first)
        List<Category> systemCategories = categoryRepository.findHierarchicalCategoriesByUserUsername(systemUsername);
        
        if (systemCategories.isEmpty()) {
            log.warn("No system categories found for user: {}", systemUsername);
            return;
        }
        
        // Map to track original category ID -> new category entity and ID for parent relationships
        Map<String, Category> categoryEntityMapping = new HashMap<>();
        Map<String, String> categoryIdMapping = new HashMap<>();
        
        // First pass: Create all categories without parent relationships
        for (Category systemCategory : systemCategories) {
            Category newCategory = Category.builder()
                    .user(newUser)
                    .name(systemCategory.getName())
                    .color(systemCategory.getColor())
                    .categoryType(systemCategory.getCategoryType())
                    .iconUrl(systemCategory.getIconUrl())
                    .parentCategory(null) // Will be set in second pass
                    .systemAction(true) // Mark as system action for audit trail
                    .build();
            
            // Ensure ID is null to prevent duplicate key exceptions - ID will be generated on persist
            newCategory.setId(null);
            Category savedCategory = categoryRepository.save(newCategory);
            categoryEntityMapping.put(systemCategory.getId(), savedCategory);
            categoryIdMapping.put(systemCategory.getId(), savedCategory.getId());
            
            log.debug("Created category '{}' for user '{}'", systemCategory.getName(), newUser.getUsername());
        }
        
        // Second pass: Update parent relationships using the saved entities from first pass
        for (Category systemCategory : systemCategories) {
            if (systemCategory.getParentCategory() != null) {
                Category newCategory = categoryEntityMapping.get(systemCategory.getId());
                Category newParentCategory = categoryEntityMapping.get(systemCategory.getParentCategory().getId());
                
                if (newCategory != null && newParentCategory != null) {
                    newCategory.setParentCategory(newParentCategory);
                    // Use save to update the relationship - entity is already managed
                    categoryRepository.save(newCategory);
                    log.debug("Set parent relationship for category '{}'", systemCategory.getName());
                }
            }
        }
        
        log.info("Successfully duplicated {} system categories for user: {}", 
                systemCategories.size(), newUser.getUsername());
    }
}
