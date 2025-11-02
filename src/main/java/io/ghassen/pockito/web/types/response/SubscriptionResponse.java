package io.ghassen.pockito.web.types.response;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for Subscription entity.
 * 
 * Used for API responses, providing complete subscription information
 * including computed fields like monthlyEquivalentAmount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    /**
     * Subscription unique identifier.
     */
    private String id;

    /**
     * Username of the subscription owner.
     */
    private String username;

    /**
     * Subscription name.
     */
    private String name;

    /**
     * URL to the subscription's icon/image.
     */
    private String iconUrl;

    /**
     * Frequency unit of recurrence (DAILY, WEEKLY, MONTHLY, YEARLY).
     */
    private SubscriptionFrequency frequency;

    /**
     * Interval - repeat every interval units of frequency.
     */
    private Integer interval;

    /**
     * Subscription amount.
     */
    private BigDecimal amount;

    /**
     * Currency code for the subscription.
     */
    private CurrencyCode currency;

    /**
     * Start date of the subscription.
     */
    private LocalDate startDate;

    /**
     * Next due date for the subscription charge.
     */
    private LocalDate nextDueDate;

    /**
     * End date of the subscription (if applicable).
     */
    private LocalDate endDate;

    /**
     * Whether the subscription is currently active.
     */
    private Boolean isActive;

    /**
     * Category ID for the subscription expense.
     */
    private String categoryId;

    /**
     * Category name (derived field).
     */
    private String categoryName;

    /**
     * Day of month (1-31) for precise scheduling.
     */
    private Integer dayOfMonth;

    /**
     * Day of week (1-7, 1=Monday, 7=Sunday) for precise scheduling.
     */
    private DayOfWeek dayOfWeek;

    /**
     * Month of year (1-12) for precise scheduling.
     */
    private MonthOfYear monthOfYear;

    /**
     * Default wallet ID to charge the subscription from.
     */
    private String defaultWalletId;

    /**
     * Default wallet name (derived field).
     */
    private String defaultWalletName;

    /**
     * Optional note/description for the subscription.
     */
    private String note;

    /**
     * When the subscription was created.
     */
    private Instant createdAt;

    /**
     * When the subscription was last updated.
     */
    private Instant updatedAt;

    /**
     * Monthly equivalent amount (computed field).
     * This is a read-only field calculated based on frequency, interval, and amount.
     * Normalized monthly cost estimation for this subscription.
     */
    private BigDecimal monthlyEquivalentAmount;
}

