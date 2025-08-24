package io.ghassen.pockito.config;

import io.ghassen.pockito.security.JwtUserSyncInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for registering interceptors and other web-related beans.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUserSyncInterceptor jwtUserSyncInterceptor;

    public WebConfig(JwtUserSyncInterceptor jwtUserSyncInterceptor) {
        this.jwtUserSyncInterceptor = jwtUserSyncInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the JWT user sync interceptor to run on all API requests
        registry.addInterceptor(jwtUserSyncInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**");
    }
}
