package io.ghassen.pockito.domain.enums;

/**
 * Enum representing the frequency unit for subscription recurrence.
 * 
 * This enum defines the unit of recurrence for subscriptions:
 * - DAILY: Recurrence happens every N days (interval)
 * - WEEKLY: Recurrence happens every N weeks (interval)
 * - MONTHLY: Recurrence happens every N months (interval)
 * - YEARLY: Recurrence happens every N years (interval)
 * 
 * The interval field in SubscriptionEntity determines how many units to wait
 * between each recurrence (e.g., frequency=WEEKLY, interval=2 means every 2 weeks).
 */
public enum SubscriptionFrequency {
    
    /**
     * Daily recurrence - subscription recurs every N days.
     */
    DAILY,
    
    /**
     * Weekly recurrence - subscription recurs every N weeks.
     */
    WEEKLY,
    
    /**
     * Monthly recurrence - subscription recurs every N months.
     */
    MONTHLY,
    
    /**
     * Yearly recurrence - subscription recurs every N years.
     */
    YEARLY
}

