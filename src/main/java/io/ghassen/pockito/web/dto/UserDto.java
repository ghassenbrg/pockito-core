package io.ghassen.pockito.web.dto;

import io.ghassen.pockito.domain.Country;
import io.ghassen.pockito.domain.CurrencyCode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Data Transfer Object for User entity.
 * 
 * Used for API responses, excluding sensitive audit information
 * while providing essential user data.
 */
@Data
@Builder
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
