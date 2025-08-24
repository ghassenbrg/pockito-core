package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.AppUser;
import io.ghassen.pockito.repo.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

  @Mock
  private AppUserRepository appUserRepository;

  @InjectMocks
  private AppUserService appUserService;

  private JwtAuthenticationToken createJwtToken() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("preferred_username", "testuser");
    claims.put("sub", "testuser");
    claims.put("email", "test@example.com");
    claims.put("name", "Test User");
    claims.put("given_name", "Test");
    claims.put("family_name", "User");
    claims.put("email_verified", true);

    Jwt jwt = mock(Jwt.class);
    lenient().when(jwt.getClaims()).thenReturn(claims);
    lenient().when(jwt.getClaimAsString("preferred_username")).thenReturn("testuser");
    lenient().when(jwt.getSubject()).thenReturn("testuser");

    JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
    lenient().when(jwtToken.getToken()).thenReturn(jwt);
    return jwtToken;
  }

  @Test
  void getOrCreateUserFromJwt_ShouldCreateNewUser_WhenUserDoesNotExist() {
    // Given
    JwtAuthenticationToken jwtToken = createJwtToken();
    when(appUserRepository.findActiveById("testuser")).thenReturn(Optional.empty());
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
      AppUser user = invocation.getArgument(0);
      // Set timestamps manually to avoid PrePersist issues
      user.setCreatedAt(java.time.Instant.now());
      user.setUpdatedAt(java.time.Instant.now());
      user.setCreatedBy("testuser");
      user.setUpdatedBy("testuser");
      return user;
    });

    // When
    AppUser result = appUserService.getOrCreateUserFromJwt(jwtToken);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo("testuser");
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    assertThat(result.getDisplayName()).isEqualTo("Test User");
    assertThat(result.getGivenName()).isEqualTo("Test");
    assertThat(result.getFamilyName()).isEqualTo("User");
    assertThat(result.getEmailVerified()).isTrue();

    verify(appUserRepository).findActiveById("testuser");
    verify(appUserRepository).save(any(AppUser.class));
  }

  @Test
  void getOrCreateUserFromJwt_ShouldReturnExistingUser_WhenUserExists() {
    // Given
    JwtAuthenticationToken jwtToken = createJwtToken();
    AppUser existingUser = AppUser.builder()
        .id("testuser")
        .email("test@example.com")
        .displayName("Test User")
        .givenName("Test")
        .familyName("User")
        .emailVerified(true)
        .build();
    existingUser.setCreatedAt(java.time.Instant.now());
    existingUser.setUpdatedAt(java.time.Instant.now());

    when(appUserRepository.findActiveById("testuser")).thenReturn(Optional.of(existingUser));

    // When
    AppUser result = appUserService.getOrCreateUserFromJwt(jwtToken);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).isEqualTo(existingUser);
    verify(appUserRepository).findActiveById("testuser");
    verify(appUserRepository, never()).save(any(AppUser.class));
  }

  @Test
  void getOrCreateUserFromJwt_ShouldUpdateUser_WhenUserInfoHasChanged() {
    // Given
    JwtAuthenticationToken jwtToken = createJwtToken();
    AppUser existingUser = AppUser.builder()
        .id("testuser")
        .email("old@example.com")
        .displayName("Old Name")
        .build();
    existingUser.setCreatedAt(java.time.Instant.now());
    existingUser.setUpdatedAt(java.time.Instant.now());

    when(appUserRepository.findActiveById("testuser")).thenReturn(Optional.of(existingUser));
    when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
      AppUser user = invocation.getArgument(0);
      user.setUpdatedAt(java.time.Instant.now());
      user.setUpdatedBy("testuser");
      return user;
    });

    // When
    AppUser result = appUserService.getOrCreateUserFromJwt(jwtToken);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail()).isEqualTo("test@example.com");
    assertThat(result.getDisplayName()).isEqualTo("Test User");

    verify(appUserRepository).findActiveById("testuser");
    verify(appUserRepository).save(any(AppUser.class));
  }

  @Test
  void updateUser_ShouldSaveAndReturnUser() {
    // Given
    AppUser user = AppUser.builder()
        .id("testuser")
        .email("test@example.com")
        .build();
    user.setCreatedAt(java.time.Instant.now());
    user.setUpdatedAt(java.time.Instant.now());

    when(appUserRepository.save(user)).thenReturn(user);

    // When
    AppUser result = appUserService.updateUser(user);

    // Then
    assertThat(result).isEqualTo(user);
    verify(appUserRepository).save(user);
  }

  @Test
  void getUserById_ShouldReturnUser_WhenUserExists() {
    // Given
    AppUser user = AppUser.builder()
        .id("testuser")
        .email("test@example.com")
        .build();
    user.setCreatedAt(java.time.Instant.now());
    user.setUpdatedAt(java.time.Instant.now());

    when(appUserRepository.findActiveById("testuser")).thenReturn(Optional.of(user));

    // When
    Optional<AppUser> result = appUserService.getUserById("testuser");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(user);
    verify(appUserRepository).findActiveById("testuser");
  }

  @Test
  void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
    // Given
    when(appUserRepository.findActiveById("testuser")).thenReturn(Optional.empty());

    // When
    Optional<AppUser> result = appUserService.getUserById("testuser");

    // Then
    assertThat(result).isEmpty();
    verify(appUserRepository).findActiveById("testuser");
  }
}
