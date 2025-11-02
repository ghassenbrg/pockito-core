package io.ghassen.pockito.user.application.service;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.category.infrastructure.persistence.repository.CategoryRepository;
import io.ghassen.pockito.user.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public User getOrCreateUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            log.debug("User with username '{}' already exists, returning existing user", username);
            return existingUser.get();
        }

        log.info("Creating new user with username '{}'", username);
        User newUser = User.builder()
                .username(username)
                .systemAction(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Successfully created user with username '{}'", username);
        
        duplicateSystemCategoriesForUser(savedUser);
        
        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public Optional<User> updateCountry(String username, String countryCode) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    try {
                        user.setCountry(io.ghassen.pockito.domain.enums.Country.fromCode(countryCode));
                        user.setSystemAction(true);
                        return userRepository.save(user);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid country code '{}' for user '{}': {}", countryCode, username, e.getMessage());
                        return user;
                    }
                });
    }

    @Transactional
    public Optional<User> updateDefaultCurrency(String username, String currencyCode) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    try {
                        user.setDefaultCurrency(io.ghassen.pockito.domain.enums.CurrencyCode.fromCode(currencyCode));
                        user.setSystemAction(true);
                        return userRepository.save(user);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid currency code '{}' for user '{}': {}", currencyCode, username, e.getMessage());
                        return user;
                    }
                });
    }

    @Transactional
    public void duplicateSystemCategoriesForUser(User newUser) {
        final String systemUsername = "system";
        log.info("Duplicating system categories for new user: {}", newUser.getUsername());
        
        List<Category> systemCategories = categoryRepository.findHierarchicalCategoriesByUserUsername(systemUsername);
        
        if (systemCategories.isEmpty()) {
            log.warn("No system categories found for user: {}", systemUsername);
            return;
        }
        
        Map<String, Category> categoryEntityMapping = new HashMap<>();
        Map<String, String> categoryIdMapping = new HashMap<>();
        
        for (Category systemCategory : systemCategories) {
            Category newCategory = Category.builder()
                    .user(newUser)
                    .name(systemCategory.getName())
                    .color(systemCategory.getColor())
                    .categoryType(systemCategory.getCategoryType())
                    .iconUrl(systemCategory.getIconUrl())
                    .parentCategory(null)
                    .systemAction(true)
                    .build();
            
            newCategory.setId(null);
            Category savedCategory = categoryRepository.save(newCategory);
            categoryEntityMapping.put(systemCategory.getId(), savedCategory);
            categoryIdMapping.put(systemCategory.getId(), savedCategory.getId());
            
            log.debug("Created category '{}' for user '{}'", systemCategory.getName(), newUser.getUsername());
        }
        
        for (Category systemCategory : systemCategories) {
            if (systemCategory.getParentCategory() != null) {
                Category newCategory = categoryEntityMapping.get(systemCategory.getId());
                Category newParentCategory = categoryEntityMapping.get(systemCategory.getParentCategory().getId());
                
                if (newCategory != null && newParentCategory != null) {
                    newCategory.setParentCategory(newParentCategory);
                    categoryRepository.save(newCategory);
                    log.debug("Set parent relationship for category '{}'", systemCategory.getName());
                }
            }
        }
        
        log.info("Successfully duplicated {} system categories for user: {}", 
                systemCategories.size(), newUser.getUsername());
    }
}


