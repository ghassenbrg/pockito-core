package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.AppUser;
import io.ghassen.pockito.repo.AppUserRepository;
import io.ghassen.pockito.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppUserService {

  private final AppUserRepository appUserRepository;

  /**
   * Gets or creates a user from JWT token claims.
   * This method automatically synchronizes user information from the JWT token.
   * 
   * @param jwtToken The JWT authentication token
   * @return The user entity (either existing or newly created)
   */
  public AppUser getOrCreateUserFromJwt(JwtAuthenticationToken jwtToken) {
    Map<String, Object> claims = jwtToken.getToken().getClaims();
    
    String userId = jwtToken.getToken().getClaimAsString("preferred_username");
    if (userId == null) {
      userId = jwtToken.getToken().getSubject();
    }
    
    if (userId == null) {
      throw new IllegalStateException("JWT token does not contain a valid user identifier");
    }
    
    // Try to find existing user
    Optional<AppUser> existingUser = appUserRepository.findActiveById(userId);
    
    if (existingUser.isPresent()) {
      AppUser user = existingUser.get();
      // Update user information if it has changed
      if (hasUserInfoChanged(user, claims)) {
        user = updateUserFromJwt(user, claims);
        log.info("Updated user information for user: {}", userId);
      }
      return user;
    } else {
      // Create new user
      AppUser newUser = createUserFromJwt(claims);
      log.info("Created new user: {}", userId);
      return newUser;
    }
  }

  /**
   * Gets the current authenticated user.
   * Automatically creates/updates the user if needed.
   * 
   * @return The current user entity
   */
  public AppUser getCurrentUser() {
    if (!SecurityUtils.isAuthenticated()) {
      throw new IllegalStateException("User is not authenticated");
    }
    
    // Get JWT token from security context
    JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) SecurityUtils.getCurrentAuthentication();
    return getOrCreateUserFromJwt(jwtToken);
  }

  /**
   * Gets user information by ID.
   * 
   * @param userId The user ID
   * @return Optional containing the user if found
   */
  @Transactional(readOnly = true)
  public Optional<AppUser> getUserById(String userId) {
    return appUserRepository.findActiveById(userId);
  }

  /**
   * Gets user information by email.
   * 
   * @param email The user email
   * @return Optional containing the user if found
   */
  @Transactional(readOnly = true)
  public Optional<AppUser> getUserByEmail(String email) {
    return appUserRepository.findActiveByEmail(email);
  }

  /**
   * Updates an existing user entity.
   * 
   * @param user The user entity to update
   * @return The updated user entity
   */
  public AppUser updateUser(AppUser user) {
    return appUserRepository.save(user);
  }

  /**
   * Updates user information from JWT claims.
   * 
   * @param user The existing user entity
   * @param claims The JWT claims
   * @return The updated user entity
   */
  private AppUser updateUserFromJwt(AppUser user, Map<String, Object> claims) {
    user.setEmail(getClaimAsString(claims, "email"));
    user.setDisplayName(getClaimAsString(claims, "name"));
    user.setGivenName(getClaimAsString(claims, "given_name"));
    user.setFamilyName(getClaimAsString(claims, "family_name"));
    
    Boolean emailVerified = getClaimAsBoolean(claims, "email_verified");
    if (emailVerified != null) {
      user.setEmailVerified(emailVerified);
    }
    
    return appUserRepository.save(user);
  }

  /**
   * Creates a new user from JWT claims.
   * 
   * @param claims The JWT claims
   * @return The newly created user entity
   */
  private AppUser createUserFromJwt(Map<String, Object> claims) {
    String userId = getClaimAsString(claims, "preferred_username");
    if (userId == null) {
      userId = getClaimAsString(claims, "sub");
    }
    
    AppUser user = AppUser.builder()
        .id(userId)
        .email(getClaimAsString(claims, "email"))
        .displayName(getClaimAsString(claims, "name"))
        .givenName(getClaimAsString(claims, "given_name"))
        .familyName(getClaimAsString(claims, "family_name"))
        .emailVerified(getClaimAsBoolean(claims, "email_verified"))
        .build();
    
    return appUserRepository.save(user);
  }

  /**
   * Checks if user information has changed compared to JWT claims.
   * 
   * @param user The existing user entity
   * @param claims The JWT claims
   * @return true if information has changed, false otherwise
   */
  private boolean hasUserInfoChanged(AppUser user, Map<String, Object> claims) {
    String email = getClaimAsString(claims, "email");
    String name = getClaimAsString(claims, "name");
    String givenName = getClaimAsString(claims, "given_name");
    String familyName = getClaimAsString(claims, "family_name");
    Boolean emailVerified = getClaimAsBoolean(claims, "email_verified");
    
    return !equals(user.getEmail(), email) ||
           !equals(user.getDisplayName(), name) ||
           !equals(user.getGivenName(), givenName) ||
           !equals(user.getFamilyName(), familyName) ||
           !equals(user.getEmailVerified(), emailVerified);
  }

  /**
   * Safely extracts a string claim from JWT claims.
   * 
   * @param claims The JWT claims
   * @param key The claim key
   * @return The claim value as string, or null if not found
   */
  private String getClaimAsString(Map<String, Object> claims, String key) {
    Object value = claims.get(key);
    return value != null ? value.toString() : null;
  }

  /**
   * Safely extracts a boolean claim from JWT claims.
   * 
   * @param claims The JWT claims
   * @param key The claim key
   * @return The claim value as boolean, or null if not found
   */
  private Boolean getClaimAsBoolean(Map<String, Object> claims, String key) {
    Object value = claims.get(key);
    if (value instanceof Boolean) {
      return (Boolean) value;
    } else if (value instanceof String) {
      return Boolean.valueOf((String) value);
    }
    return null;
  }

  /**
   * Safely compares two objects for equality, handling null values.
   * 
   * @param a First object
   * @param b Second object
   * @return true if objects are equal, false otherwise
   */
  private boolean equals(Object a, Object b) {
    if (a == b) return true;
    if (a == null || b == null) return false;
    return a.equals(b);
  }
}
