package io.ghassen.pockito.user.web.api.response;

import io.ghassen.pockito.domain.enums.Country;
import io.ghassen.pockito.domain.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse  {

    private String username;

    private Country country;

    private CurrencyCode defaultCurrency;

    private Instant createdAt;

    private Instant updatedAt;
}


