package io.ghassen.pockito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class AuditingConfig {

  @Bean
  public AuditorAware<UUID> auditorAware() {
    return () -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth instanceof JwtAuthenticationToken token && auth.isAuthenticated()) {
        try {
          return Optional.of(UUID.fromString(token.getToken().getSubject()));
        } catch (Exception ignored) {}
      }
      return Optional.empty();
    };
  }
}
