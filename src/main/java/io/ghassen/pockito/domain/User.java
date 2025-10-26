package io.ghassen.pockito.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.ghassen.pockito.domain.enums.Country;
import io.ghassen.pockito.domain.enums.CurrencyCode;

/**
 * User entity representing a registered application user.
 * 
 * This entity is created on the first backend call authenticated by a Keycloak token.
 * The username is extracted from the Keycloak token's preferred_username claim and
 * serves as the primary key.
 * 
 * Entity behavior:
 * - When a user connects with a Keycloak token:
 *   - If username does not exist → create a new User with username
 *   - If username exists → load existing user (no duplication)
 * - Token-derived transient data (e.g., email, roles, claims) must not be persisted
 * 
 * Future extensibility:
 * - Additional fields (e.g., locale, preferences) may be added later
 */
@Entity
@Table(name = "app_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends AuditableEntityNoId {

    /**
     * Primary key - extracted from Keycloak token (preferred_username claim).
     * Immutable once set.
     */
    @Id
    @NotNull
    @Column(name = "username", nullable = false, updatable = false, length = 255)
    @EqualsAndHashCode.Include
    private String username;

    /**
     * User's country of residence.
     * Nullable but must match enum if present.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "country", length = 3)
    private Country country;

    /**
     * User's preferred default currency.
     * Nullable but must match enum if present.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_currency", length = 3)
    private CurrencyCode defaultCurrency;

    // Note: No setter for username to ensure immutability
    // The username field is only set during construction and cannot be modified
}
