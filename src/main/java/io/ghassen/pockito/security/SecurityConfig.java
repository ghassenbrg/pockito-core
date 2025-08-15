package io.ghassen.pockito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
      );
    return http.build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> KeycloakRealmRoleConverter.from(jwt));
    return converter;
  }
}
