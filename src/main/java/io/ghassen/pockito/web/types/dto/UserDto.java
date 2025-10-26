package io.ghassen.pockito.web.types.dto;

import io.ghassen.pockito.domain.enums.Country;
import io.ghassen.pockito.domain.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Internal DTO for User entity.
 * 
 * Used for transferring user data between the web layer and service layer.
 * Contains all necessary fields for internal processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /**
     * User's username (primary identifier)
     */
    private String username;

    /**
     * User's country of residence
     */
    private Country country;

    /**
     * User's preferred default currency
     */
    private CurrencyCode defaultCurrency;

    /**
     * When the user was created
     */
    private Instant createdAt;

    /**
     * When the user was last updated
     */
    private Instant updatedAt;
}
