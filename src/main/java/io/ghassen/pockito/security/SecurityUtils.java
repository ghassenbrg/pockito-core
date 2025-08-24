package io.ghassen.pockito.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Centralized utility for extracting user information from JWT tokens.
 * This class provides consistent user ID extraction across all services.
 * 
 * <h3>Key Design Principle:</h3>
 * <p>This utility returns user identifiers as <strong>strings directly</strong> from JWT claims,
 * not converted to UUIDs. This gives you the raw string value from Keycloak (e.g., "gbargougui")
 * which is more readable and useful for logging, display, and audit purposes.</p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <h4>In Service Classes:</h4>
 * <pre>{@code
 * @Service
 * public class SomeService {
 *   public void someMethod() {
 *     // Get string user ID for logging and display
 *     String userId = SecurityUtils.getCurrentUserId();
 *     
 *     // Use userId for logging, display, and database operations
 *     log.info("Processing request for user: {}", userId);
 *     List<Entity> entities = repo.findByUserId(userId);
 *   }
 * }
 * }</pre>
 * 
 * <h4>In Controllers:</h4>
 * <pre>{@code
 * @RestController
 * public class SomeController {
 *   @GetMapping("/data")
 *   public ResponseEntity<?> getData() {
 *     String userId = SecurityUtils.getCurrentUserId();
 *     // Return data for the current user
 *   }
 * }
 * }</pre>
 * 
 * <h4>In Repository Methods:</h4>
 * <pre>{@code
 * @Repository
 * public interface SomeRepository extends JpaRepository<Entity, UUID> {
 *   @Query("SELECT e FROM Entity e WHERE e.userId = :userId")
 *   List<Entity> findByUserId(@Param("userId") String userId);
 * }
 * 
 * // In service:
 * List<Entity> entities = repo.findByUserId(SecurityUtils.getCurrentUserId());
 * }</pre>
 * 
 * <h3>JWT Claim Priority:</h3>
 * <ol>
 *   <li><strong>preferred_username</strong> - Primary identifier (Keycloak standard) - Returns as string directly</li>
 *   <li><strong>sub</strong> - Fallback identifier (OIDC standard) - Returns as string directly</li>
 * </ol>
 * 
 * <h3>When to Use Which Method:</h3>
 * <ul>
 *   <li><strong>getCurrentUserId()</strong> - For logging, display, audit fields, API responses, and database operations</li>
 *   <li><strong>getCurrentUsername()</strong> - For display and logging purposes</li>
 *   <li><strong>getCurrentUserEmail()</strong> - For notifications and user identification</li>
 * </ul>
 * 
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li>Throws {@link IllegalStateException} if no valid JWT token is found</li>
 *   <li>Throws {@link IllegalStateException} if required claims are missing</li>
 *   <li>Provides detailed error messages for debugging</li>
 * </ul>
 */
@Component
@Slf4j
public class SecurityUtils {

  /**
   * Extracts the current user ID from the JWT token.
   * Uses preferred_username as the primary identifier, falling back to subject if needed.
   * 
   * <p><strong>Returns the raw string value directly from JWT claims.</strong>
   * This is the recommended method for most use cases like logging, display, and audit fields.</p>
   * 
   * <p>Example: If your JWT contains "preferred_username": "gbargougui", this method returns "gbargougui"</p>
   * 
   * @return The user ID as a string (preferred_username or subject)
   * @throws IllegalStateException if no valid user ID can be extracted
   */
  public static String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth instanceof JwtAuthenticationToken jwt) {
      // Try preferred_username first (Keycloak standard)
      String username = jwt.getToken().getClaimAsString("preferred_username");
      if (username != null && !username.trim().isEmpty()) {
        return username.trim();
      }
      
      // Fallback to subject claim
      String subject = jwt.getToken().getSubject();
      if (subject != null && !subject.trim().isEmpty()) {
        return subject.trim();
      }
      
      log.error("No valid user ID found in JWT token. Available claims: {}", jwt.getToken().getClaims());
      throw new IllegalStateException("JWT token does not contain a valid user identifier. Please ensure the token contains a 'preferred_username' or 'sub' claim.");
    }
    
    log.error("Authentication is not a JWT token. Authentication type: {}, Principal: {}", 
              auth != null ? auth.getClass().getSimpleName() : "null", 
              auth != null ? auth.getPrincipal() : "null");
    throw new IllegalStateException("No valid JWT authentication found. Please ensure you are authenticated with a valid JWT token.");
  }

  /**
   * Extracts the current username from the JWT token.
   * 
   * @return The username as a string
   * @throws IllegalStateException if no valid username can be extracted
   */
  public static String getCurrentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth instanceof JwtAuthenticationToken jwt) {
      // Try preferred_username first
      String username = jwt.getToken().getClaimAsString("preferred_username");
      if (username != null && !username.trim().isEmpty()) {
        return username.trim();
      }
      
      // Fallback to subject
      String subject = jwt.getToken().getSubject();
      if (subject != null && !subject.trim().isEmpty()) {
        return subject.trim();
      }
      
      log.error("No valid username found in JWT token. Available claims: {}", jwt.getToken().getClaims());
      throw new IllegalStateException("JWT token does not contain a valid username.");
    }
    
    log.error("Authentication is not a JWT token. Authentication type: {}, Principal: {}", 
              auth != null ? auth.getClass().getSimpleName() : "null", 
              auth != null ? auth.getPrincipal() : "null");
    throw new IllegalStateException("No valid JWT authentication found.");
  }

  /**
   * Checks if the current user is authenticated.
   * 
   * @return true if authenticated, false otherwise
   */
  public static boolean isAuthenticated() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.isAuthenticated() && auth instanceof JwtAuthenticationToken;
  }

  /**
   * Extracts the current user's email from the JWT token.
   * 
   * @return The email as a string, or null if not available
   */
  public static String getCurrentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth instanceof JwtAuthenticationToken jwt) {
      return jwt.getToken().getClaimAsString("email");
    }
    
    return null;
  }

  /**
   * Gets all available claims from the current JWT token for debugging purposes.
   * 
   * @return A map of all JWT claims, or null if no JWT token is available
   */
  public static java.util.Map<String, Object> getCurrentUserClaims() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth instanceof JwtAuthenticationToken jwt) {
      return jwt.getToken().getClaims();
    }
    
    return null;
  }

  /**
   * Gets the current authentication object from the security context.
   * 
   * @return The current authentication object
   * @throws IllegalStateException if no authentication is found
   */
  public static Authentication getCurrentAuthentication() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth == null || !auth.isAuthenticated()) {
      throw new IllegalStateException("No valid authentication found");
    }
    
    return auth;
  }
}
