package io.ghassen.pockito.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.ghassen.pockito.domain.converter.DayOfWeekConverter;
import io.ghassen.pockito.domain.converter.MonthOfYearConverter;
import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import io.ghassen.pockito.web.validation.SubscriptionId;
import jakarta.persistence.Convert;

/**
 * Subscription entity representing a recurring subscription expense.
 * 
 * A subscription represents a recurring expense that happens at regular intervals.
 * Each subscription has a frequency (DAILY, WEEKLY, MONTHLY, YEARLY) and an interval
 * that determines how often the subscription recurs.
 * 
 * Entity behavior:
 * - Each user can have multiple subscriptions
 * - Subscriptions are linked to expense categories
 * - Subscriptions have a default wallet for automatic billing
 * - nextDueDate is calculated based on frequency and interval
 * - dayOfMonth, dayOfWeek, and monthOfYear are optional and used for precise scheduling
 */
@Entity
@Table(
    name = "t_subscription",
    indexes = {
        @Index(columnList = "user_id", name = "idx_subscription_user_id"),
        @Index(columnList = "category_id", name = "idx_subscription_category"),
        @Index(columnList = "default_wallet_id", name = "idx_subscription_default_wallet"),
        @Index(columnList = "is_active", name = "idx_subscription_is_active"),
        @Index(columnList = "next_due_date", name = "idx_subscription_next_due_date")
    }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SubscriptionId
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Subscription extends AuditableEntity {

    /**
     * The user who owns this subscription.
     * Required relationship, cannot be null.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "username",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_subscription_user")
    )
    @NotNull
    private User user;

    /**
     * Subscription name.
     * Required field with length validation.
     */
    @Column(name = "name", nullable = false, length = 200)
    @NotBlank
    @Size(min = 1, max = 200)
    private String name;

    /**
     * URL to the subscription's icon/image.
     * Optional field with length validation.
     */
    @Column(name = "icon_url", length = 500)
    @Size(max = 500)
    private String iconUrl;

    /**
     * Frequency unit of recurrence (DAILY, WEEKLY, MONTHLY, YEARLY).
     * Required field to determine the unit of recurrence.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 10)
    @NotNull
    private SubscriptionFrequency frequency;

    /**
     * Interval - repeat every interval units of frequency.
     * Example: every 2 weeks → frequency=WEEKLY, interval=2
     * Required field, must be positive.
     */
    @Column(name = "interval", nullable = false)
    @NotNull
    @Min(value = 1, message = "Interval must be at least 1")
    private Integer interval;

    /**
     * Subscription amount.
     * Required field with precision validation (17,2).
     */
    @Column(name = "amount", nullable = false, precision = 17, scale = 2)
    @NotNull
    @Digits(integer = 15, fraction = 2)
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /**
     * Currency code for the subscription.
     * Required field using ISO-4217 standard.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    @NotNull
    private CurrencyCode currency;

    /**
     * Start date of the subscription.
     * Required field to determine when the subscription started.
     */
    @Column(name = "start_date", nullable = false)
    @NotNull
    private LocalDate startDate;

    /**
     * Next due date for the subscription charge.
     * Optional field, calculated based on frequency and interval.
     * Can be null if the subscription has ended (endDate is set before the nextDueDate).
     */
    @Column(name = "next_due_date", nullable = true)
    private LocalDate nextDueDate;

    /**
     * Last payment date for the subscription.
     * Optional field, set when a payment is successfully processed and transaction is created.
     * Only updated when pay service API is successful and transaction created.
     */
    @Column(name = "last_payment_date", nullable = true)
    private LocalDate lastPaymentDate;

    /**
     * End date of the subscription (if applicable).
     * Optional field - null means subscription continues indefinitely.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Whether the subscription is currently active.
     * Required field, defaults to true.
     */
    @Column(name = "is_active", nullable = false)
    @NotNull
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Category ID for the subscription expense.
     * Required relationship to categorize the subscription.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "category_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_subscription_category")
    )
    @NotNull
    private Category categoryId;

    /**
     * Day of month (1-31) for precise scheduling (e.g., bill on 15th of each month).
     * Optional field - only used for MONTHLY frequency.
     */
    @Column(name = "day_of_month")
    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    private Integer dayOfMonth;

    /**
     * Day of week (1-7, 1=Monday, 7=Sunday) for precise scheduling.
     * Optional field - only used for WEEKLY frequency.
     */
    @Column(name = "day_of_week")
    @Convert(converter = DayOfWeekConverter.class)
    private DayOfWeek dayOfWeek;

    /**
     * Month of year (1-12) for precise scheduling (e.g., bill in January).
     * Optional field - only used for YEARLY frequency.
     */
    @Column(name = "month_of_year")
    @Convert(converter = MonthOfYearConverter.class)
    private MonthOfYear monthOfYear;

    /**
     * Default wallet ID to charge the subscription from.
     * Required relationship for automatic billing.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "default_wallet_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_subscription_default_wallet")
    )
    @NotNull
    private Wallet defaultWalletId;

    /**
     * Optional note/description for the subscription.
     * Optional field with length validation.
     */
    @Column(name = "note", length = 1000)
    @Size(max = 1000)
    private String note;
}

