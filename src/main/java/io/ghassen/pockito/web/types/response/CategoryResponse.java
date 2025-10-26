package io.ghassen.pockito.web.types.response;

import io.ghassen.pockito.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Category entity.
 * 
 * Used for API responses, providing complete category information
 * including hierarchical relationships.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * Category unique identifier.
     */
    private UUID id;

    /**
     * Username of the category owner.
     */
    private String username;

    /**
     * Category name.
     */
    private String name;

    /**
     * Description of the category.
     */
    private String description;

    /**
     * Hex color code for the category.
     */
    private String color;

    /**
     * Type of the category - either EXPENSE or INCOME.
     */
    private CategoryType categoryType;

    /**
     * URL to the category's icon/image.
     */
    private String iconUrl;

    /**
     * ID of the parent category for hierarchical organization.
     * Null for root categories.
     */
    private UUID parentCategoryId;

    /**
     * Name of the parent category.
     */
    private String parentCategoryName;

    /**
     * When the category was created.
     */
    private Instant createdAt;

    /**
     * When the category was last updated.
     */
    private Instant updatedAt;

    /**
     * Whether the category is active (not archived).
     */
    private Boolean active;

    /**
     * Number of child categories.
     */
    private Integer childCount;
}
