package io.ghassen.pockito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

@Configuration
public class AuditingConfig {

  @Bean
  public AuditorAware<String> auditorAware() {
    return () -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth instanceof JwtAuthenticationToken token && auth.isAuthenticated()) {
        try {
          // Extract username from JWT claims (preferred_username or email)
          String username = token.getToken().getClaimAsString("preferred_username");
          if (username == null) {
            username = token.getToken().getClaimAsString("email");
          }
          if (username == null) {
            username = token.getToken().getSubject();
          }
          return Optional.of(username);
        } catch (Exception ignored) {}
      }
      return Optional.empty();
    };
  }
}
