package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.CategoryType;
import io.ghassen.pockito.service.CategoryService;
import io.ghassen.pockito.web.dto.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for category operations.
 * 
 * Provides HTTP endpoints for category CRUD operations and management.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing user categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category for the authenticated user.
     * 
     * @param categoryDto the category data to create
     * @return the created category
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new category",
        description = "Creates a new category for the authenticated user. Category names must be unique per user."
    )
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto categoryDto) {
        
        log.info("Creating category: {}", categoryDto.getName());
        
        // Username is automatically set by the service from SecurityUtils
        CategoryDto createdCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Get all categories for the authenticated user.
     * 
     * @return list of user's categories
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all categories",
        description = "Retrieves all categories for the authenticated user, ordered by name."
    )
    public ResponseEntity<List<CategoryDto>> getUserCategories() {
        
        log.debug("Getting all categories for authenticated user");
        
        List<CategoryDto> categories = categoryService.getUserCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get categories by type for the authenticated user.
     * 
     * @param categoryType the category type to filter by
     * @return list of categories of the specified type
     */
    @GetMapping("/type/{categoryType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get categories by type",
        description = "Retrieves all categories of a specific type (EXPENSE or INCOME) for the authenticated user."
    )
    public ResponseEntity<List<CategoryDto>> getCategoriesByType(
            @Parameter(description = "Category type to filter by") 
            @PathVariable CategoryType categoryType) {
        
        log.debug("Getting categories of type {} for authenticated user", categoryType);
        
        List<CategoryDto> categories = categoryService.getUserCategoriesByType(categoryType);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get hierarchical categories for the authenticated user.
     * 
     * @return list of categories in hierarchical order
     */
    @GetMapping("/hierarchical")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get hierarchical categories",
        description = "Retrieves all categories for the authenticated user in hierarchical order (parents first, then children)."
    )
    public ResponseEntity<List<CategoryDto>> getHierarchicalCategories() {
        
        log.debug("Getting hierarchical categories for authenticated user");
        
        List<CategoryDto> categories = categoryService.getHierarchicalCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get hierarchical categories by type for the authenticated user.
     * 
     * @param categoryType the category type to filter by
     * @return list of categories of the specified type in hierarchical order
     */
    @GetMapping("/hierarchical/type/{categoryType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get hierarchical categories by type",
        description = "Retrieves all categories of a specific type for the authenticated user in hierarchical order."
    )
    public ResponseEntity<List<CategoryDto>> getHierarchicalCategoriesByType(
            @Parameter(description = "Category type to filter by") 
            @PathVariable CategoryType categoryType) {
        
        log.debug("Getting hierarchical categories of type {} for authenticated user", categoryType);
        
        List<CategoryDto> categories = categoryService.getHierarchicalCategoriesByType(categoryType);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific category by ID for the authenticated user.
     * 
     * @param categoryId the category ID
     * @return the category if found and owned by user
     */
    @GetMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a specific category by its ID for the authenticated user."
    )
    public ResponseEntity<CategoryDto> getCategory(
            @Parameter(description = "Category ID") 
            @PathVariable UUID categoryId) {
        
        log.debug("Getting category with ID: {}", categoryId);
        
        return categoryService.getCategoryById(categoryId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get child categories of a specific parent category.
     * 
     * @param parentCategoryId the parent category ID
     * @return list of child categories
     */
    @GetMapping("/{parentCategoryId}/children")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get child categories",
        description = "Retrieves all child categories of a specific parent category for the authenticated user."
    )
    public ResponseEntity<List<CategoryDto>> getChildCategories(
            @Parameter(description = "Parent category ID") 
            @PathVariable UUID parentCategoryId) {
        
        log.debug("Getting child categories for parent ID: {}", parentCategoryId);
        
        List<CategoryDto> categories = categoryService.getChildCategories(parentCategoryId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get root categories (no parent) for the authenticated user.
     * 
     * @return list of root categories
     */
    @GetMapping("/root")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get root categories",
        description = "Retrieves all root categories (categories without a parent) for the authenticated user."
    )
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        
        log.debug("Getting root categories for authenticated user");
        
        List<CategoryDto> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get categories by color for the authenticated user.
     * 
     * @param color the color to filter by
     * @return list of categories with the specified color
     */
    @GetMapping("/color/{color}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get categories by color",
        description = "Retrieves all categories with a specific color for the authenticated user."
    )
    public ResponseEntity<List<CategoryDto>> getCategoriesByColor(
            @Parameter(description = "Color to filter by (hex format, e.g., #A1B2C3)") 
            @PathVariable String color) {
        
        log.debug("Getting categories with color: {}", color);
        
        List<CategoryDto> categories = categoryService.getCategoriesByColor(color);
        return ResponseEntity.ok(categories);
    }

    /**
     * Update an existing category for the authenticated user.
     * 
     * @param categoryId the category ID to update
     * @param categoryDto the updated category data
     * @return the updated category
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update category",
        description = "Updates an existing category for the authenticated user. Category names must remain unique per user."
    )
    public ResponseEntity<CategoryDto> updateCategory(
            @Parameter(description = "Category ID to update") 
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryDto categoryDto) {
        
        log.info("Updating category with ID: {}", categoryId);
        
        // Username is automatically set by the service from SecurityUtils and cannot be updated
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(categoryId, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a category for the authenticated user.
     * 
     * @param categoryId the category ID to delete
     * @return no content on success
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete category",
        description = "Deletes a category for the authenticated user. Category must not have child categories."
    )
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID to delete") 
            @PathVariable UUID categoryId) {
        
        log.info("Deleting category with ID: {}", categoryId);
        
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
