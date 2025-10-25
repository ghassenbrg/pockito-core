package io.ghassen.pockito.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.ghassen.pockito.domain.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Category entity.
 * 
 * Used for transferring category data between the web layer and service layer.
 * Includes validation annotations to ensure data integrity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    /**
     * Category unique identifier.
     */
    private UUID id;

    /**
     * Username of the category owner.
     * This field is automatically set from the authenticated user and cannot be
     * updated.
     */
    private String username;

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
    private UUID parentCategoryId;

    /**
     * Name of the parent category (for display purposes).
     * This is a derived field populated by the service.
     */
    private String parentCategoryName;

    /**
     * Whether the category is active (not archived).
     * Derived from archivedAt field - true if archivedAt is null, false otherwise.
     */
    private Boolean active;

    /**
     * Number of child categories (for display purposes).
     * This is a derived field populated by the service.
     */
    private Integer childCount;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    private Instant updatedAt;
}
