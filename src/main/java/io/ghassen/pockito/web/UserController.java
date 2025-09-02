package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.service.UserService;
import io.ghassen.pockito.web.dto.UserDto;
import io.ghassen.pockito.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for user management operations.
 * 
 * Provides endpoints for user creation, retrieval, and updates.
 * Implements the core requirement to create users on first Keycloak token authentication.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing application users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Get or create a user based on the authenticated user's username.
     * 
     * This endpoint implements the core requirement:
     * - If username does not exist → create a new User with username
     * - If username exists → load existing user (no duplication)
     * 
     * The username is extracted from the authenticated Keycloak token.
     * 
     * @return the existing or newly created user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get or create current user",
        description = "Retrieves the current authenticated user or creates a new one if it doesn't exist. " +
                     "This implements the core requirement to create users on first Keycloak token authentication."
    )
    public ResponseEntity<UserDto> getOrCreateCurrentUser() {
        
        // the username from the authenticated user's token
        String username = SecurityUtils.getCurrentUserId();
        
        log.info("Getting or creating user for username: {}", username);
        User user = userService.getOrCreateUser(username);
        
        UserDto userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Get a user by username.
     * 
     * @param username the username to search for
     * @return the user if found, 404 if not found
     */
    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user by username",
        description = "Retrieves a user by their username"
    )
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username to search for") 
            @PathVariable String username) {
        
        log.debug("Looking up user with username: {}", username);
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isPresent()) {
            UserDto userDto = userMapper.toDto(user.get());
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a user's country.
     * 
     * @param username the username of the user to update
     * @param countryCode the new country code (alpha-2 or alpha-3)
     * @return the updated user if successful, 404 if user not found, 400 if invalid country code
     */
    @PutMapping("/{username}/country")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user country",
        description = "Updates the country for a specific user. Accepts both alpha-2 and alpha-3 country codes."
    )
    public ResponseEntity<UserDto> updateUserCountry(
            @Parameter(description = "Username of the user to update") 
            @PathVariable String username,
            @Parameter(description = "Country code (alpha-2 or alpha-3)") 
            @RequestParam String countryCode) {
        
        log.info("Updating country to '{}' for user: {}", countryCode, username);
        Optional<User> updatedUser = userService.updateCountry(username, countryCode);
        
        if (updatedUser.isPresent()) {
            UserDto userDto = userMapper.toDto(updatedUser.get());
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a user's default currency.
     * 
     * @param username the username of the user to update
     * @param currencyCode the new currency code (3-letter ISO code)
     * @return the updated user if successful, 404 if user not found, 400 if invalid currency code
     */
    @PutMapping("/{username}/currency")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user default currency",
        description = "Updates the default currency for a specific user. Accepts 3-letter ISO currency codes."
    )
    public ResponseEntity<UserDto> updateUserCurrency(
            @Parameter(description = "Username of the user to update") 
            @PathVariable String username,
            @Parameter(description = "Currency code (3-letter ISO)") 
            @RequestParam String currencyCode) {
        
        log.info("Updating default currency to '{}' for user: {}", currencyCode, username);
        Optional<User> updatedUser = userService.updateDefaultCurrency(username, currencyCode);
        
        if (updatedUser.isPresent()) {
            UserDto userDto = userMapper.toDto(updatedUser.get());
            return ResponseEntity.ok(userDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a user exists by username.
     * 
     * @param username the username to check
     * @return 200 if user exists, 404 if not found
     */
    @GetMapping("/{username}/exists")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Check if user exists",
        description = "Checks whether a user with the specified username exists"
    )
    public ResponseEntity<Void> checkUserExists(
            @Parameter(description = "Username to check") 
            @PathVariable String username) {
        
        log.debug("Checking if user exists: {}", username);
        boolean exists = userService.existsByUsername(username);
        
        if (exists) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
