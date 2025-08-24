package io.ghassen.pockito.security;

import io.ghassen.pockito.domain.AppUser;
import io.ghassen.pockito.repo.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Optional;

/**
 * Interceptor that automatically synchronizes users from Keycloak JWT tokens
 * to the local app_user table when they first authenticate.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUserSyncInterceptor implements HandlerInterceptor {

    private final AppUserRepository appUserRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only process authenticated requests
        if (request.getUserPrincipal() instanceof JwtAuthenticationToken jwtToken) {
            try {
                syncUserFromJwt(jwtToken);
            } catch (Exception e) {
                log.warn("Failed to sync user from JWT: {}", e.getMessage());
                // Don't fail the request, just log the warning
            }
        }
        return true;
    }

    private void syncUserFromJwt(JwtAuthenticationToken jwtToken) {
        String userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        
        if (userId == null || email == null) {
            log.debug("Missing required user information in JWT for user sync");
            return;
        }

        // Check if user already exists
        Optional<AppUser> existingUser = appUserRepository.findById(userId);
        if (existingUser.isPresent()) {
            log.debug("User {} already exists in database", userId);
            return;
        }

        // Create new user from JWT claims
        try {
            AppUser newUser = AppUser.builder()
                .id(userId)
                .email(email)
                .displayName(extractDisplayName(jwtToken))
                .givenName(jwtToken.getToken().getClaimAsString("given_name"))
                .familyName(jwtToken.getToken().getClaimAsString("family_name"))
                .emailVerified(jwtToken.getToken().getClaimAsBoolean("email_verified"))
                .locale(jwtToken.getToken().getClaimAsString("locale"))
                .timezone(jwtToken.getToken().getClaimAsString("zoneinfo"))
                .createdBy("system")
                .updatedBy("system")
                .build();

            appUserRepository.save(newUser);
            log.info("Successfully created new user {} from JWT authentication", userId);
            
        } catch (Exception e) {
            log.error("Failed to create user {} from JWT: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private String extractDisplayName(JwtAuthenticationToken jwtToken) {
        // Try to construct display name from available claims
        String givenName = jwtToken.getToken().getClaimAsString("given_name");
        String familyName = jwtToken.getToken().getClaimAsString("family_name");
        String preferredUsername = jwtToken.getToken().getClaimAsString("preferred_username");
        
        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        } else if (givenName != null) {
            return givenName;
        } else if (familyName != null) {
            return familyName;
        } else if (preferredUsername != null) {
            return preferredUsername;
        } else {
            return "Unknown User";
        }
    }
}
