package io.ghassen.pockito.domain.enums;

/**
 * Enum representing the month of the year for subscription scheduling.
 * 
 * This enum defines the months of the year for precise scheduling:
 * - JANUARY (1): First month of the year
 * - FEBRUARY (2): Second month of the year
 * - MARCH (3): Third month of the year
 * - APRIL (4): Fourth month of the year
 * - MAY (5): Fifth month of the year
 * - JUNE (6): Sixth month of the year
 * - JULY (7): Seventh month of the year
 * - AUGUST (8): Eighth month of the year
 * - SEPTEMBER (9): Ninth month of the year
 * - OCTOBER (10): Tenth month of the year
 * - NOVEMBER (11): Eleventh month of the year
 * - DECEMBER (12): Last month of the year
 * 
 * Used for YEARLY frequency subscriptions to specify which month of the year
 * the subscription should recur on.
 */
public enum MonthOfYear {
    
    /**
     * January - first month of the year (value: 1).
     */
    JANUARY(1),
    
    /**
     * February - second month of the year (value: 2).
     */
    FEBRUARY(2),
    
    /**
     * March - third month of the year (value: 3).
     */
    MARCH(3),
    
    /**
     * April - fourth month of the year (value: 4).
     */
    APRIL(4),
    
    /**
     * May - fifth month of the year (value: 5).
     */
    MAY(5),
    
    /**
     * June - sixth month of the year (value: 6).
     */
    JUNE(6),
    
    /**
     * July - seventh month of the year (value: 7).
     */
    JULY(7),
    
    /**
     * August - eighth month of the year (value: 8).
     */
    AUGUST(8),
    
    /**
     * September - ninth month of the year (value: 9).
     */
    SEPTEMBER(9),
    
    /**
     * October - tenth month of the year (value: 10).
     */
    OCTOBER(10),
    
    /**
     * November - eleventh month of the year (value: 11).
     */
    NOVEMBER(11),
    
    /**
     * December - last month of the year (value: 12).
     */
    DECEMBER(12);
    
    private final int value;
    
    /**
     * Constructor for MonthOfYear enum.
     * 
     * @param value the integer value representing the month (1-12)
     */
    MonthOfYear(int value) {
        this.value = value;
    }
    
    /**
     * Gets the integer value of the month of year.
     * 
     * @return the integer value (1-12) where 1=January, 12=December
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Converts an integer value to the corresponding MonthOfYear enum.
     * 
     * @param value the integer value (1-12)
     * @return the corresponding MonthOfYear enum, or null if value is invalid
     */
    public static MonthOfYear fromValue(int value) {
        for (MonthOfYear month : MonthOfYear.values()) {
            if (month.value == value) {
                return month;
            }
        }
        return null;
    }
    
    /**
     * Converts an integer value to the corresponding MonthOfYear enum.
     * 
     * @param value the integer value (1-12), can be null
     * @return the corresponding MonthOfYear enum, or null if value is null or invalid
     */
    public static MonthOfYear fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return fromValue(value.intValue());
    }
}

