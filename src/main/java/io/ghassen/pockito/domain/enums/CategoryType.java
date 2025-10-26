package io.ghassen.pockito.domain.enums;

/**
 * Enum representing the type of category.
 * 
 * Categories can be either for expenses (money going out) or income (money coming in).
 * This distinction helps users organize their financial transactions appropriately.
 */
public enum CategoryType {
    
    /**
     * Category for expenses - money going out of the user's accounts.
     * Examples: Groceries, Rent, Utilities, Entertainment, etc.
     */
    EXPENSE,
    
    /**
     * Category for income - money coming into the user's accounts.
     * Examples: Salary, Freelance, Investment Returns, Gifts, etc.
     */
    INCOME
}
