package io.ghassen.pockito.domain.enums;

/**
 * Enum representing the day of the week for subscription scheduling.
 * 
 * This enum defines the days of the week for precise scheduling:
 * - MONDAY (1): First day of the week
 * - TUESDAY (2): Second day of the week
 * - WEDNESDAY (3): Third day of the week
 * - THURSDAY (4): Fourth day of the week
 * - FRIDAY (5): Fifth day of the week
 * - SATURDAY (6): Sixth day of the week
 * - SUNDAY (7): Last day of the week
 * 
 * Used for WEEKLY frequency subscriptions to specify which day of the week
 * the subscription should recur on.
 */
public enum DayOfWeek {
    
    /**
     * Monday - first day of the week (value: 1).
     */
    MONDAY(1),
    
    /**
     * Tuesday - second day of the week (value: 2).
     */
    TUESDAY(2),
    
    /**
     * Wednesday - third day of the week (value: 3).
     */
    WEDNESDAY(3),
    
    /**
     * Thursday - fourth day of the week (value: 4).
     */
    THURSDAY(4),
    
    /**
     * Friday - fifth day of the week (value: 5).
     */
    FRIDAY(5),
    
    /**
     * Saturday - sixth day of the week (value: 6).
     */
    SATURDAY(6),
    
    /**
     * Sunday - last day of the week (value: 7).
     */
    SUNDAY(7);
    
    private final int value;
    
    /**
     * Constructor for DayOfWeek enum.
     * 
     * @param value the integer value representing the day (1-7)
     */
    DayOfWeek(int value) {
        this.value = value;
    }
    
    /**
     * Gets the integer value of the day of week.
     * 
     * @return the integer value (1-7) where 1=Monday, 7=Sunday
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Converts an integer value to the corresponding DayOfWeek enum.
     * 
     * @param value the integer value (1-7)
     * @return the corresponding DayOfWeek enum, or null if value is invalid
     */
    public static DayOfWeek fromValue(int value) {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.value == value) {
                return day;
            }
        }
        return null;
    }
    
    /**
     * Converts an integer value to the corresponding DayOfWeek enum.
     * 
     * @param value the integer value (1-7), can be null
     * @return the corresponding DayOfWeek enum, or null if value is null or invalid
     */
    public static DayOfWeek fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return fromValue(value.intValue());
    }
}

