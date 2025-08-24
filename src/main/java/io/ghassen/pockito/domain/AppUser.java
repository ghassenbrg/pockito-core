package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "app_user")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class AppUser extends AuditableEntityNoId {

  @Id
  @Column(name = "id", length = 255, nullable = false)
  private String id; // Keycloak sub (preferred_username)

  @Column(name = "email", nullable = false, unique = true, columnDefinition = "CITEXT")
  private String email;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "locale", length = 10)
  private String locale;

  @Column(name = "timezone", length = 64)
  private String timezone;

  @Column(name = "default_currency", length = 3)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String defaultCurrency;

  // Additional fields from JWT token
  @Column(name = "given_name")
  private String givenName;

  @Column(name = "family_name")
  private String familyName;

  @Column(name = "email_verified")
  private Boolean emailVerified;

  // Helper methods
  public String getFullName() {
    if (givenName != null && familyName != null) {
      return givenName + " " + familyName;
    } else if (displayName != null) {
      return displayName;
    } else if (givenName != null) {
      return givenName;
    } else if (familyName != null) {
      return familyName;
    } else {
      return email;
    }
  }

  public boolean isEmailVerified() {
    return emailVerified != null && emailVerified;
  }
}
