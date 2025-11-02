package io.ghassen.pockito.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityUtils {

  public static String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    if (auth instanceof JwtAuthenticationToken jwt) {
      String username = jwt.getToken().getClaimAsString("preferred_username");
      if (username != null && !username.trim().isEmpty()) {
        return username.trim();
      }
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

  public static String getCurrentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwt) {
      String username = jwt.getToken().getClaimAsString("preferred_username");
      if (username != null && !username.trim().isEmpty()) {
        return username.trim();
      }
      String subject = jwt.getToken().getSubject();
      if (subject != null && !subject.trim().isEmpty()) {
        return subject.trim();
      }
      throw new IllegalStateException("JWT token does not contain a valid username");
    }
    throw new IllegalStateException("No valid JWT authentication found");
  }
}


