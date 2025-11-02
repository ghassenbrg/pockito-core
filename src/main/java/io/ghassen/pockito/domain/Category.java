package io.ghassen.pockito.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.validation.CategoryId;

/**
 * Category entity representing a user-defined category for organizing transactions or other entities.
 * 
 * Categories can be hierarchical with parent-child relationships and are owned by specific users.
 * Each category has a name, color, type (expense/income), optional icon, and can reference a parent category.
 * 
 * Entity behavior:
 * - Each user can have multiple categories
 * - Category names must be unique per user
 * - Categories can have parent categories (self-referencing relationship)
 * - Categories must have a type (EXPENSE or INCOME)
 * - Color must be a valid hex color code
 * - Icon URL is optional but must be valid if provided
 */
@Entity
@Table(
    name = "t_category",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"}, name = "uk_category_user_name")
    },
    indexes = {
        @Index(columnList = "user_id", name = "idx_category_user_id"),
        @Index(columnList = "parent_category_id", name = "idx_category_parent_id")
    }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@CategoryId
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Category extends AuditableEntity {

    /**
     * The user who owns this category.
     * Required relationship, cannot be null.
     */
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "username",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_category_user")
    )
    @NotNull
    private User user;

    /**
     * Category name - must be unique per user.
     * Required field with length validation.
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    /**
     * Hex color code for the category (e.g., #A1B2C3).
     * Required field with hex color validation.
     */
    @Column(name = "color", nullable = false, length = 7)
    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #A1B2C3)")
    @Size(min = 7, max = 7)
    private String color;

    /**
     * Type of the category - either EXPENSE or INCOME.
     * Required field to distinguish between expense and income categories.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 10)
    @NotNull
    private CategoryType categoryType;

    /**
     * URL to the category's icon/image.
     * Optional field with length validation.
     */
    @Column(name = "icon_url", length = 500)
    @Size(max = 500)
    private String iconUrl;

    /**
     * Reference to the parent category for hierarchical organization.
     * Optional field for creating category hierarchies.
     */
    @ManyToOne(optional = true)
    @JoinColumn(
        name = "parent_category_id",
        referencedColumnName = "id",
        nullable = true,
        foreignKey = @ForeignKey(name = "fk_category_parent")
    )
    private Category parentCategory;
}
