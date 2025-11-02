package io.ghassen.pockito.category.web.controller;

import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.validation.ValidationGroups;
import io.ghassen.pockito.category.application.service.CategoryService;
import io.ghassen.pockito.category.application.dto.CategoryDto;
import io.ghassen.pockito.category.web.api.request.CategoryRequest;
import io.ghassen.pockito.category.web.api.response.CategoryResponse;
import io.ghassen.pockito.category.web.api.response.CategoryListResponse;
import io.ghassen.pockito.category.web.mapper.CategoryApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing user categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper categoryMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new category",
        description = "Creates a new category for the authenticated user. Category names must be unique per user."
    )
    public ResponseEntity<CategoryResponse> createCategory(
            @Validated(ValidationGroups.Create.class) @RequestBody CategoryRequest categoryRequest) {

        log.info("Creating category: {}", categoryRequest.getName());

        CategoryDto categoryDto = categoryMapper.requestToDto(categoryRequest);

        CategoryDto createdCategoryDto = categoryService.createCategory(categoryDto);

        CategoryResponse categoryResponse = categoryMapper.dtoToResponse(createdCategoryDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all categories",
        description = "Retrieves all categories for the authenticated user, ordered by name."
    )
    public ResponseEntity<CategoryListResponse> getUserCategories() {

        log.debug("Getting all categories for authenticated user");

        List<CategoryDto> categoryDtos = categoryService.getUserCategories();

        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categoryDtos);

        CategoryListResponse response = CategoryListResponse.builder()
            .categories(categoryResponses)
            .totalCount((long) categoryResponses.size())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{categoryType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get categories by type",
        description = "Retrieves all categories of a specific type (EXPENSE or INCOME) for the authenticated user."
    )
    public ResponseEntity<CategoryListResponse> getCategoriesByType(
            @Parameter(description = "Category type to filter by") 
            @PathVariable CategoryType categoryType) {

        log.debug("Getting categories of type {} for authenticated user", categoryType);

        List<CategoryDto> categoryDtos = categoryService.getUserCategoriesByType(categoryType);

        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categoryDtos);

        CategoryListResponse response = CategoryListResponse.builder()
            .categories(categoryResponses)
            .totalCount((long) categoryResponses.size())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/hierarchical")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get hierarchical categories",
        description = "Retrieves all categories for the authenticated user in hierarchical order (parents first, then children)."
    )
    public ResponseEntity<CategoryListResponse> getHierarchicalCategories() {

        log.debug("Getting hierarchical categories for authenticated user");

        List<CategoryDto> categories = categoryService.getHierarchicalCategories();
        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categories);
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount((long) categoryResponses.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hierarchical/type/{categoryType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get hierarchical categories by type",
        description = "Retrieves all categories of a specific type for the authenticated user in hierarchical order."
    )
    public ResponseEntity<CategoryListResponse> getHierarchicalCategoriesByType(
            @Parameter(description = "Category type to filter by") 
            @PathVariable CategoryType categoryType) {

        log.debug("Getting hierarchical categories of type {} for authenticated user", categoryType);

        List<CategoryDto> categories = categoryService.getHierarchicalCategoriesByType(categoryType);
        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categories);
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount((long) categoryResponses.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a specific category by its ID for the authenticated user."
    )
    public ResponseEntity<CategoryResponse> getCategory(
            @Parameter(description = "Category ID") 
            @PathVariable String categoryId) {

        log.debug("Getting category with ID: {}", categoryId);

        return categoryService.getCategoryById(categoryId)
            .map(categoryMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{parentCategoryId}/children")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get child categories",
        description = "Retrieves all child categories of a specific parent category for the authenticated user."
    )
    public ResponseEntity<CategoryListResponse> getChildCategories(
            @Parameter(description = "Parent category ID") 
            @PathVariable String parentCategoryId) {

        log.debug("Getting child categories for parent ID: {}", parentCategoryId);

        List<CategoryDto> categories = categoryService.getChildCategories(parentCategoryId);
        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categories);
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount((long) categoryResponses.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/root")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get root categories",
        description = "Retrieves all root categories (categories without a parent) for the authenticated user."
    )
    public ResponseEntity<CategoryListResponse> getRootCategories() {

        log.debug("Getting root categories for authenticated user");

        List<CategoryDto> categories = categoryService.getRootCategories();
        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categories);
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount((long) categoryResponses.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/color/{color}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get categories by color",
        description = "Retrieves all categories with a specific color for the authenticated user."
    )
    public ResponseEntity<CategoryListResponse> getCategoriesByColor(
            @Parameter(description = "Color to filter by (hex format, e.g., #A1B2C3)") 
            @PathVariable String color) {

        log.debug("Getting categories with color: {}", color);

        List<CategoryDto> categories = categoryService.getCategoriesByColor(color);
        List<CategoryResponse> categoryResponses = categoryMapper.dtoListToResponseList(categories);
        CategoryListResponse response = CategoryListResponse.builder()
                .categories(categoryResponses)
                .totalCount((long) categoryResponses.size())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update category",
        description = "Updates an existing category for the authenticated user. Category names must remain unique per user."
    )
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID to update") 
            @PathVariable String categoryId,
            @Validated(ValidationGroups.Update.class) @RequestBody CategoryRequest categoryRequest) {

        log.info("Updating category with ID: {}", categoryId);

        CategoryDto categoryDto = categoryMapper.requestToDto(categoryRequest);

        try {
            CategoryDto updatedCategoryDto = categoryService.updateCategory(categoryId, categoryDto);

            CategoryResponse categoryResponse = categoryMapper.dtoToResponse(updatedCategoryDto);

            return ResponseEntity.ok(categoryResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update category: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete category",
        description = "Deletes a category for the authenticated user. Category must not have child categories."
    )
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID to delete") 
            @PathVariable String categoryId) {

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


