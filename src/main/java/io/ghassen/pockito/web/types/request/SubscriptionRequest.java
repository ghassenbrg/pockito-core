package io.ghassen.pockito.web.types.request;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import io.ghassen.pockito.web.validation.CategoryId;
import io.ghassen.pockito.web.validation.WalletId;
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

/**
 * Request DTO for creating and updating subscriptions.
 * 
 * Used as API request payload for subscription operations.
 * Excludes fields that are automatically managed by the system (id, nextDueDate).
 * nextDueDate can be provided or will be calculated based on frequency and interval.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    /**
     * Subscription name.
     */
    @NotBlank(message = "Subscription name is required")
    @Size(min = 1, max = 200, message = "Subscription name must be between 1 and 200 characters")
    private String name;

    /**
     * URL to the subscription's icon/image.
     */
    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    /**
     * Frequency unit of recurrence (DAILY, WEEKLY, MONTHLY, YEARLY).
     */
    @NotNull(message = "Frequency is required")
    private SubscriptionFrequency frequency;

    /**
     * Interval - repeat every interval units of frequency.
     * Example: every 2 weeks → frequency=WEEKLY, interval=2
     */
    @NotNull(message = "Interval is required")
    @Min(value = 1, message = "Interval must be at least 1")
    private Integer interval;

    /**
     * Subscription amount.
     */
    @NotNull(message = "Amount is required")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 15 digits and 2 decimal places")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Currency code for the subscription.
     */
    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    /**
     * Start date of the subscription.
     */
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /**
     * Next due date for the subscription charge.
     * Optional - if not provided, will be calculated based on frequency and interval from startDate.
     */
    // private LocalDate nextDueDate;

    /**
     * End date of the subscription (if applicable).
     * Optional - null means subscription continues indefinitely.
     */
    private LocalDate endDate;

    /**
     * Whether the subscription is currently active.
     */
    @NotNull(message = "Is active is required")
    private Boolean isActive;

    /**
     * Category ID for the subscription expense.
     */
    @CategoryId
    @NotNull(message = "Category ID is required")
    private String categoryId;

    /**
     * Day of month (1-31) for precise scheduling (e.g., bill on 15th of each month).
     * Optional - only used for MONTHLY frequency.
     */
    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    private Integer dayOfMonth;

    /**
     * Day of week (1-7, 1=Monday, 7=Sunday) for precise scheduling.
     * Optional - only used for WEEKLY frequency.
     */
    private DayOfWeek dayOfWeek;

    /**
     * Month of year (1-12) for precise scheduling (e.g., bill in January).
     * Optional - only used for YEARLY frequency.
     */
    private MonthOfYear monthOfYear;

    /**
     * Default wallet ID to charge the subscription from.
     */
    @WalletId
    @NotNull(message = "Default wallet ID is required")
    private String defaultWalletId;

    /**
     * Optional note/description for the subscription.
     */
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}

