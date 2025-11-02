package io.ghassen.pockito.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;

public final class KeycloakRealmRoleConverter {
  private KeycloakRealmRoleConverter(){}

  @SuppressWarnings("unchecked")
  public static Collection<GrantedAuthority> from(Jwt jwt) {
    Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims()
        .getOrDefault("realm_access", Map.of());
    Collection<String> roles = (Collection<String>) realmAccess
        .getOrDefault("roles", List.of());
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String r : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase(Locale.ROOT)));
    }
    return authorities;
  }

  public static JwtAuthenticationToken createAuthentication(Jwt jwt) {
    Collection<GrantedAuthority> authorities = from(jwt);
    
    String username = jwt.getClaimAsString("preferred_username");
    if (username == null) {
      username = jwt.getSubject();
    }
    
    return new JwtAuthenticationToken(jwt, authorities, username);
  }
}


