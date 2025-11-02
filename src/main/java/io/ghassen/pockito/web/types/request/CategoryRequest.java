package io.ghassen.pockito.web.types.request;

import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.validation.CategoryId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating and updating categories.
 * 
 * Used as API request payload for category operations.
 * Excludes fields that are automatically managed by the system (id, username).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    /**
     * Category name - must be unique per user.
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    /**
     * Hex color code for the category (e.g., #A1B2C3).
     */
    @NotBlank(message = "Color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #A1B2C3)")
    @Size(min = 7, max = 7, message = "Color must be exactly 7 characters including the # symbol")
    private String color;

    /**
     * Type of the category - either EXPENSE or INCOME.
     */
    @NotNull(message = "Category type is required")
    private CategoryType categoryType;

    /**
     * URL to the category's icon/image.
     */
    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    /**
     * ID of the parent category for hierarchical organization.
     * Null for root categories.
     */
    @CategoryId
    private String parentCategoryId;
}
