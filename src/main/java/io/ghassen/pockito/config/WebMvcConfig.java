package io.ghassen.pockito.config;

import io.ghassen.pockito.security.JwtUserSyncInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for registering interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final JwtUserSyncInterceptor jwtUserSyncInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // Register JWT user sync interceptor for all API endpoints
    registry.addInterceptor(jwtUserSyncInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/public/**"); // Exclude public endpoints if any
  }
}
