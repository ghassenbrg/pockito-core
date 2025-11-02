package io.ghassen.pockito.user.web.controller;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.validation.ValidationGroups;
import io.ghassen.pockito.shared.security.SecurityUtils;
import io.ghassen.pockito.user.application.service.UserService;
import io.ghassen.pockito.user.application.dto.UserDto;
import io.ghassen.pockito.user.web.api.response.UserResponse;
import io.ghassen.pockito.user.web.mapper.UserApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing application users")
public class UserController {

    private final UserService userService;
    private final UserApiMapper userMapper;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get or create current user",
        description = "Retrieves the current authenticated user or creates a new one if it doesn't exist. " +
                     "This implements the core requirement to create users on first Keycloak token authentication."
    )
    public ResponseEntity<UserResponse> getOrCreateCurrentUser() {
        
        String username = SecurityUtils.getCurrentUserId();
        
        log.info("Getting or creating user for username: {}", username);
        User user = userService.getOrCreateUser(username);
        
        UserDto userDto = userMapper.toDto(user);
        UserResponse userResponse = userMapper.toResponse(userDto);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user by username",
        description = "Retrieves a user by their username"
    )
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Username to search for") 
            @PathVariable String username) {
        
        log.debug("Looking up user with username: {}", username);
        Optional<User> user = userService.findByUsername(username);
        
        if (user.isPresent()) {
            UserDto userDto = userMapper.toDto(user.get());
            return ResponseEntity.ok(userMapper.toResponse(userDto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{username}/country")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user country",
        description = "Updates the country for a specific user. Accepts both alpha-2 and alpha-3 country codes."
    )
    public ResponseEntity<UserResponse> updateUserCountry(
            @Parameter(description = "Username of the user to update") 
            @PathVariable String username,
            @Parameter(description = "Country code (alpha-2 or alpha-3)") 
            @Validated(ValidationGroups.Update.class) @RequestParam String countryCode) {
        
        log.info("Updating country to '{}' for user: {}", countryCode, username);
        Optional<User> updatedUser = userService.updateCountry(username, countryCode);
        
        if (updatedUser.isPresent()) {
            UserDto userDto = userMapper.toDto(updatedUser.get());
            return ResponseEntity.ok(userMapper.toResponse(userDto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{username}/currency")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update user default currency",
        description = "Updates the default currency for a specific user. Accepts 3-letter ISO currency codes."
    )
    public ResponseEntity<UserResponse> updateUserCurrency(
            @Parameter(description = "Username of the user to update") 
            @PathVariable String username,
            @Parameter(description = "Currency code (3-letter ISO)") 
            @Validated(ValidationGroups.Update.class) @RequestParam String currencyCode) {
        
        log.info("Updating default currency to '{}' for user: {}", currencyCode, username);
        Optional<User> updatedUser = userService.updateDefaultCurrency(username, currencyCode);
        
        if (updatedUser.isPresent()) {
            UserDto userDto = userMapper.toDto(updatedUser.get());
            return ResponseEntity.ok(userMapper.toResponse(userDto));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

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


