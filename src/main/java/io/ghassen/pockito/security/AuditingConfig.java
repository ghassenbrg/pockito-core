package io.ghassen.pockito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditingConfig {

  @Bean
  public AuditorAware<String> auditorAware() {
    return () -> {
      try {
        String username = SecurityUtils.getCurrentUsername();
        return Optional.of(username);
      } catch (Exception e) {
        // Log the error but don't fail the request
        // This is just for auditing purposes
        return Optional.empty();
      }
    };
  }
}
