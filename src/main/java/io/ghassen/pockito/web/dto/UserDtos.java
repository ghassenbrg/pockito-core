package io.ghassen.pockito.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.ghassen.pockito.domain.AppUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTOs for user operations.
 */
public class UserDtos {

  /**
   * User information response DTO.
   */
  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class UserInfoResponse {
    private String id;
    private String email;
    private String displayName;
    private String givenName;
    private String familyName;
    private String fullName;
    private Boolean emailVerified;
    private String locale;
    private String timezone;
    private String defaultCurrency;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserInfoResponse fromEntity(AppUser user) {
      return UserInfoResponse.builder()
          .id(user.getId())
          .email(user.getEmail())
          .displayName(user.getDisplayName())
          .givenName(user.getGivenName())
          .familyName(user.getFamilyName())
          .fullName(user.getFullName())
          .emailVerified(user.getEmailVerified())
          .locale(user.getLocale())
          .timezone(user.getTimezone())
          .defaultCurrency(user.getDefaultCurrency())
          .createdAt(user.getCreatedAt())
          .updatedAt(user.getUpdatedAt())
          .build();
    }
  }

  /**
   * Update user request DTO.
   */
  @Data
  @Builder
  public static class UpdateUserRequest {
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Locale must be in format 'en' or 'en-US'")
    private String locale;
    
    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Timezone must be in format 'America/New_York'")
    private String timezone;
    
    private String defaultCurrency;
  }

  /**
   * User profile update response DTO.
   */
  @Data
  @Builder
  public static class UserProfileUpdateResponse {
    private String message;
    private UserInfoResponse user;
    private Instant updatedAt;
  }
}
