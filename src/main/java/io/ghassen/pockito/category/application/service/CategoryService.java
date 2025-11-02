package io.ghassen.pockito.category.application.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.category.infrastructure.persistence.repository.CategoryRepository;
import io.ghassen.pockito.user.infrastructure.persistence.repository.UserRepository;
import io.ghassen.pockito.shared.security.SecurityUtils;
import io.ghassen.pockito.category.application.dto.CategoryDto;
import io.ghassen.pockito.category.application.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        String username = SecurityUtils.getCurrentUserId();
        categoryDto.setUsername(username);

        log.debug("Creating category for user: {}", username);

        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (categoryRepository.existsByUserUsernameAndName(username, categoryDto.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName()
                    + "' already exists for user: " + username);
        }

        Category category = categoryMapper.toEntity(categoryDto);
        category.setId(null);
        category.setUser(user);

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

    @Transactional(readOnly = true)
    public List<CategoryDto> getUserCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories for user: {}", username);
        List<Category> categories = categoryRepository.findByUserUsernameOrderByNameAsc(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getUserCategoriesByType(CategoryType categoryType) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories of type {} for user: {}", categoryType, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndCategoryTypeOrderByNameAsc(username, categoryType);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} categories of type {} for user: {}", categoryDtos.size(), categoryType, username);
        return categoryDtos;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getHierarchicalCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting hierarchical categories for user: {}", username);
        List<Category> categories = categoryRepository.findHierarchicalCategoriesByUserUsername(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} hierarchical categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getHierarchicalCategoriesByType(CategoryType categoryType) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting hierarchical categories of type {} for user: {}", categoryType, username);
        List<Category> categories = categoryRepository.findHierarchicalCategoriesByUserUsernameAndType(username, categoryType);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} hierarchical categories of type {} for user: {}", categoryDtos.size(), categoryType, username);
        return categoryDtos;
    }

    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(String categoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting category with ID: {} for user: {}", categoryId, username);
        Optional<CategoryDto> categoryDto = categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getUsername().equals(username))
                .map(categoryMapper::toDto);

        if (categoryDto.isPresent()) {
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

    @Transactional(readOnly = true)
    public List<CategoryDto> getChildCategories(String parentCategoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting child categories for parent ID: {} and user: {}", parentCategoryId, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndParentCategoryIdOrderByNameAsc(username, parentCategoryId);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} child categories for parent ID: {} and user: {}", categoryDtos.size(), parentCategoryId, username);
        return categoryDtos;
    }

    public CategoryDto updateCategory(String categoryId, CategoryDto categoryDto) {
        String username = SecurityUtils.getCurrentUserId();
        categoryDto.setUsername(username);

        log.debug("Updating category with ID: {} for user: {}", categoryId, username);

        Category existingCategory = categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));

        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.existsByUserUsernameAndName(username, categoryDto.getName())) {
            throw new IllegalArgumentException(
                    "Category with name '" + categoryDto.getName() + "' already exists for user: " + username);
        }

        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);

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

    public void deleteCategory(String categoryId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting category with ID: {} for user: {}", categoryId, username);

        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));

        List<Category> children = categoryRepository.findByUserUsernameAndParentCategoryId(username, categoryId);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with child categories. Please delete child categories first.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {} for user: {}", categoryId, username);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting root categories for user: {}", username);
        List<Category> categories = categoryRepository.findByUserUsernameAndParentCategoryIsNullOrderByNameAsc(username);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} root categories for user: {}", categoryDtos.size(), username);
        return categoryDtos;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByColor(String color) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting categories with color {} for user: {}", color, username);
        List<Category> categories = categoryRepository.findByUserUsernameAndColorOrderByNameAsc(username, color);
        List<CategoryDto> categoryDtos = categoryMapper.toDtoList(categories);

        for (int i = 0; i < categories.size(); i++) {
            setDerivedFields(categoryDtos.get(i), categories.get(i));
        }

        log.info("Retrieved {} categories with color {} for user: {}", categoryDtos.size(), color, username);
        return categoryDtos;
    }

    private void setDerivedFields(CategoryDto categoryDto, Category category) {
        if (categoryDto.getActive() == null) {
            categoryDto.setActive(category.getArchivedAt() == null);
        }

        if (categoryDto.getChildCount() == null) {
            List<Category> children = categoryRepository.findByUserUsernameAndParentCategoryId(
                category.getUser().getUsername(), category.getId());
            categoryDto.setChildCount(children.size());
        }
    }
}


