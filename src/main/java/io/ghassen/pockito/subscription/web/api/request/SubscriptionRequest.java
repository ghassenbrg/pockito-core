package io.ghassen.pockito.subscription.web.api.request;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import io.ghassen.pockito.domain.validation.CategoryId;
import io.ghassen.pockito.domain.validation.WalletId;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Subscription name is required")
    @Size(min = 1, max = 200, message = "Subscription name must be between 1 and 200 characters")
    private String name;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    @NotNull(message = "Frequency is required")
    private SubscriptionFrequency frequency;

    @NotNull(message = "Interval is required")
    @Min(value = 1, message = "Interval must be at least 1")
    private Integer interval;

    @NotNull(message = "Amount is required")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 digits and 2 decimal places")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Enabled is required")
    private Boolean enabled;

    @CategoryId
    @NotNull(message = "Category ID is required")
    private String categoryId;

    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    private Integer dayOfMonth;

    private DayOfWeek dayOfWeek;

    private MonthOfYear monthOfYear;

    @WalletId
    @NotNull(message = "Default wallet ID is required")
    private String defaultWalletId;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}


