package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.AppUser;
import io.ghassen.pockito.service.AppUserService;
import io.ghassen.pockito.web.dto.UserDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Controller for user-related operations.
 * Provides endpoints for user information and profile updates.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final AppUserService appUserService;

  /**
   * Gets the current user's information.
   * 
   * @return User information response
   */
  @GetMapping("/me")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<UserDtos.UserInfoResponse> getCurrentUser() {
    try {
      AppUser user = appUserService.getCurrentUser();
      UserDtos.UserInfoResponse response = UserDtos.UserInfoResponse.fromEntity(user);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to get current user: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Gets user information by ID.
   * 
   * @param userId The user ID
   * @return User information response
   */
  @GetMapping("/{userId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDtos.UserInfoResponse> getUserById(@PathVariable String userId) {
    try {
      return appUserService.getUserById(userId)
          .map(user -> ResponseEntity.ok(UserDtos.UserInfoResponse.fromEntity(user)))
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error("Failed to get user by ID {}: {}", userId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Updates the current user's profile.
   * 
   * @param request The update request
   * @return Updated user information response
   */
  @PutMapping("/me")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<UserDtos.UserProfileUpdateResponse> updateCurrentUser(
      @Valid @RequestBody UserDtos.UpdateUserRequest request) {
    try {
      AppUser user = appUserService.getCurrentUser();
      
      // Update user fields
      if (request.getLocale() != null) {
        user.setLocale(request.getLocale());
      }
      if (request.getTimezone() != null) {
        user.setTimezone(request.getTimezone());
      }
      if (request.getDefaultCurrency() != null) {
        user.setDefaultCurrency(request.getDefaultCurrency());
      }
      
      // Save updated user
      AppUser updatedUser = appUserService.updateUser(user);
      
      UserDtos.UserProfileUpdateResponse response = UserDtos.UserProfileUpdateResponse.builder()
          .message("User profile updated successfully")
          .user(UserDtos.UserInfoResponse.fromEntity(updatedUser))
          .updatedAt(Instant.now())
          .build();
      
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to update current user: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Gets user information by email.
   * 
   * @param email The user email
   * @return User information response
   */
  @GetMapping("/by-email")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDtos.UserInfoResponse> getUserByEmail(@RequestParam String email) {
    try {
      return appUserService.getUserByEmail(email)
          .map(user -> ResponseEntity.ok(UserDtos.UserInfoResponse.fromEntity(user)))
          .orElse(ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error("Failed to get user by email {}: {}", email, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
