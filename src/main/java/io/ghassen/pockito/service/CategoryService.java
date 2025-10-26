package io.ghassen.pockito.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.types.dto.CategoryDto;
import io.ghassen.pockito.web.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for category business operations.
 * 
 * Provides business logic for category management including CRUD operations,
 * validation rules, and business constraints enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Create a new category for the authenticated user.
     * 
     * @param categoryDto the category data to create
     * @return the created category DTO
     * @throws IllegalArgumentException if user not found or category name already exists
     */
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // Automatically set username from authenticated user
        String username = SecurityUtils.getCurrentUserId();
        categoryDto.setUsername(username);
        
        log.debug("Creating category for user: {}", username);

        // Validate user exists
        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if category name already exists for this user
        if (categoryRepository.existsByUserUsernameAndName(username, categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName()
                    + "' already exists for user: " + username);
        }

        // Convert to entity and save
        Category category = categoryMapper.toEntity(categoryDto);
        category.setUser(user);

        // Set parent category if specified
        if (categoryDto.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParentCategoryId())
                    .filter(parent -> parent.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found or access denied"));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category with ID: {} for user: {}", savedCategory.getId(), username);

        CategoryDto createdCategoryDto = categoryMapper.toDto(savedCategory);
        setDerivedFields(createdCategoryDto, savedCategory);
        return createdCategoryDto;
    }

    /**
     * Get all categories for the authenticated user.
     * 
     * @return list of category DTOs ordered by name
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getUserCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories for user: {}", username);
        List<Category> categories = categoryRepository.findByUserUsernameOrderByNameAsc(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    /**
     * Get categories by type for the authenticated user.
     * 
     * @param categoryType the category type to filter by
     * @return list of category DTOs of the specified type
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getUserCategoriesByType(CategoryType categoryType) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories of type {} for user: {}", categoryType, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndCategoryTypeOrderByNameAsc(username, categoryType);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} categories of type {} for user: {}", categoryDtos.size(), categoryType, username);
        return categoryDtos;
    }

    /**
     * Get hierarchical categories for the authenticated user.
     * 
     * @return list of category DTOs in hierarchical order
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getHierarchicalCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting hierarchical categories for user: {}", username);
        List<Category> categories = categoryRepository.findHierarchicalCategoriesByUserUsername(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} hierarchical categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    /**
     * Get hierarchical categories by type for the authenticated user.
     * 
     * @param categoryType the category type to filter by
     * @return list of category DTOs of the specified type in hierarchical order
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getHierarchicalCategoriesByType(CategoryType categoryType) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting hierarchical categories of type {} for user: {}", categoryType, username);
        List<Category> categories = categoryRepository.findHierarchicalCategoriesByUserUsernameAndType(username, categoryType);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} hierarchical categories of type {} for user: {}", categoryDtos.size(), categoryType, username);
        return categoryDtos;
    }

    /**
     * Get category by ID for the authenticated user.
     * 
     * @param categoryId the category ID
     * @return the category DTO if found and owned by user
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(UUID categoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting category with ID: {} for user: {}", categoryId, username);
        Optional<CategoryDto> categoryDto = categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getUsername().equals(username))
                .map(categoryMapper::toDto);
        
        if (categoryDto.isPresent()) {
            // Set derived fields for the category
            Category category = categoryRepository.findById(categoryId)
                    .filter(c -> c.getUser().getUsername().equals(username))
                    .orElse(null);
            if (category != null) {
                setDerivedFields(categoryDto.get(), category);
            }
            log.info("Retrieved category with ID: {} for user: {}", categoryId, username);
        } else {
            log.info("Category with ID: {} not found or access denied for user: {}", categoryId, username);
        }
        
        return categoryDto;
    }

    /**
     * Get child categories of a specific parent category.
     * 
     * @param parentCategoryId the parent category ID
     * @return list of child category DTOs
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(UUID parentCategoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting child categories for parent ID: {} and user: {}", parentCategoryId, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndParentCategoryIdOrderByNameAsc(username, parentCategoryId);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} child categories for parent ID: {} and user: {}", categoryDtos.size(), parentCategoryId, username);
        return categoryDtos;
    }

    /**
     * Update an existing category for the authenticated user.
     * 
     * @param categoryId  the category ID to update
     * @param categoryDto the updated category data
     * @return the updated category DTO
     * @throws IllegalArgumentException if category not found, not owned by user, or validation fails
     */
    public CategoryDto updateCategory(UUID categoryId, CategoryDto categoryDto) {
        // Automatically set username from authenticated user and prevent username updates
        String username = SecurityUtils.getCurrentUserId();
        categoryDto.setUsername(username);
        
        log.debug("Updating category with ID: {} for user: {}", categoryId, username);

        Category existingCategory = categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));

        // Check if name change would conflict with existing category
        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByUserUsernameAndName(username, categoryDto.getName())) {
            throw new IllegalArgumentException(
                    "Category with name '" + categoryDto.getName() + "' already exists for user: " + username);
        }

        // Update entity with new data
        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);

        // Handle parent category change
        if (categoryDto.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDto.getParentCategoryId())
                    .filter(parent -> parent.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found or access denied"));
            existingCategory.setParentCategory(parentCategory);
        } else {
            existingCategory.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        log.info("Updated category with ID: {} for user: {}", categoryId, username);

        CategoryDto updatedCategoryDto = categoryMapper.toDto(updatedCategory);
        setDerivedFields(updatedCategoryDto, updatedCategory);
        return updatedCategoryDto;
    }

    /**
     * Delete a category for the authenticated user.
     * 
     * @param categoryId the category ID to delete
     * @throws IllegalArgumentException if category not found, not owned by user, or has children
     */
    public void deleteCategory(UUID categoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting category with ID: {} for user: {}", categoryId, username);

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));

        // Check if category has children
        List<Category> children = categoryRepository.findByUserUsernameAndParentCategoryId(username, categoryId);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with child categories. Please delete child categories first.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {} for user: {}", categoryId, username);
    }

    /**
     * Get root categories (no parent) for the authenticated user.
     * 
     * @return list of root category DTOs
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting root categories for user: {}", username);
        List<Category> categories = categoryRepository.findByUserUsernameAndParentCategoryIsNullOrderByNameAsc(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} root categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    /**
     * Get categories by color for the authenticated user.
     * 
     * @param color the color to filter by
     * @return list of category DTOs with the specified color
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByColor(String color) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories with color {} for user: {}", color, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndColorOrderByNameAsc(username, color);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);
        
        // Set derived fields for each category
        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }
        
        log.info("Retrieved {} categories with color {} for user: {}", categoryDtos.size(), color, username);
        return categoryDtos;
    }

    /**
     * Set derived fields for a category DTO.
     * 
     * @param categoryDto the category DTO to set derived fields for
     * @param category the category entity to get information from
     */
    private void setDerivedFields(CategoryDto categoryDto, Category category) {
        // Set active based on archivedAt (true if not archived, false if archived)
        if (categoryDto.getActive() == null) {
            categoryDto.setActive(category.getArchivedAt() == null);
        }
        
        // Set child count
        if (categoryDto.getChildCount() == null) {
            List<Category> children = categoryRepository.findByUserUsernameAndParentCategoryId(
                category.getUser().getUsername(), category.getId());
            categoryDto.setChildCount(children.size());
        }
    }
}
