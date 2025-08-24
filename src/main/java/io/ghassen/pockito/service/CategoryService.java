package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.dto.CategoryDtos;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

  private final CategoryRepository categoryRepo;

  public List<Category> list(boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Listing categories for user: {} with includeArchived: {}", userId, includeArchived);
    
    if (includeArchived) {
      return categoryRepo.findByUserIdOrderByNameAsc(userId);
    } else {
      return categoryRepo.findActiveByUserId(userId);
    }
  }

  public List<Category> listByType(Category.CategoryType type, boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Listing categories by type: {} for user: {} with includeArchived: {}", type, userId, includeArchived);
    
    if (includeArchived) {
      return categoryRepo.findByUserIdAndType(userId, type);
    } else {
      return categoryRepo.findActiveByUserIdAndType(userId, type);
    }
  }

  public List<Category> searchCategories(String searchTerm, boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Searching categories for user: {} with term: '{}', includeArchived: {}", userId, searchTerm, includeArchived);
    
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return list(includeArchived);
    }
    
    if (includeArchived) {
      return categoryRepo.searchByUserIdAndTerm(userId, searchTerm.trim());
    } else {
      return categoryRepo.searchActiveByUserIdAndTerm(userId, searchTerm.trim());
    }
  }

  public Category get(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Getting category {} for user: {}", id, userId);
    return categoryRepo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @Transactional
  public Category create(CategoryDtos.CreateReq req) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Creating category '{}' of type {} for user: {}", req.name(), req.type(), userId);

    // Check for unique name within the same type for the user
    if (categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(userId, req.type(), req.name())) {
      throw new IllegalArgumentException("Category name already exists for this type");
    }

    // Validate parent category if provided
    if (req.parentId() != null) {
      Category parent = categoryRepo.findByIdAndUserId(req.parentId(), userId)
          .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
      
      // Ensure parent is not archived
      if (parent.getArchivedAt() != null) {
        throw new IllegalArgumentException("Cannot create category under archived parent");
      }
      
      // Ensure parent is of the same type
      if (parent.getType() != req.type()) {
        throw new IllegalArgumentException("Parent category must be of the same type");
      }
    }

    Category category = Category.builder()
        .userId(userId)
        .name(req.name())
        .type(req.type())
        .color(req.color())
        .iconType(req.iconType())
        .iconValue(req.iconValue())
        .parent(req.parentId() != null ? Category.builder().id(req.parentId()).build() : null)
        .build();

    category = categoryRepo.save(category);
    log.info("Created category '{}' with ID {} for user: {}", category.getName(), category.getId(), userId);
    return category;
  }

  @Transactional
  public Category update(UUID id, CategoryDtos.UpdateReq req) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Updating category {} for user: {}", id, userId);

    Category category = get(id);

    // Check for unique name within the same type for the user (excluding current category)
    if (!category.getName().equalsIgnoreCase(req.name()) &&
        categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCaseExcludingId(userId, req.type(), req.name(), id)) {
      throw new IllegalArgumentException("Category name already exists for this type");
    }

    // Validate parent category if provided
    if (req.parentId() != null) {
      Category parent = categoryRepo.findByIdAndUserId(req.parentId(), userId)
          .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
      
      // Ensure parent is not archived
      if (parent.getArchivedAt() != null) {
        throw new IllegalArgumentException("Cannot set archived category as parent");
      }
      
      // Ensure parent is of the same type
      if (parent.getType() != req.type()) {
        throw new IllegalArgumentException("Parent category must be of the same type");
      }
      
      // Prevent circular reference
      if (req.parentId().equals(id)) {
        throw new IllegalArgumentException("Category cannot be its own parent");
      }
    }

    category.setName(req.name());
    category.setType(req.type());
    category.setColor(req.color());
    category.setIconType(req.iconType());
    category.setIconValue(req.iconValue());
    category.setParent(req.parentId() != null ? Category.builder().id(req.parentId()).build() : null);

    category = categoryRepo.save(category);
    log.info("Updated category '{}' with ID {} for user: {}", req.name(), id, userId);
    return category;
  }

  @Transactional
  public void archive(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Archiving category {} for user: {}", id, userId);

    Category category = get(id);
    if (category.getArchivedAt() != null) {
      throw new IllegalArgumentException("Category is already archived");
    }

    // Check if category has active children
    List<Category> activeChildren = categoryRepo.findActiveByParentId(id);
    if (!activeChildren.isEmpty()) {
      throw new IllegalArgumentException("Cannot archive category with active children. Please archive or move the children first.");
    }

    // Archive the category
    category.setArchivedAt(Instant.now());
    category.setArchivedBy(userId);
    categoryRepo.save(category);

    log.info("Archived category '{}' with ID {} for user: {}", category.getName(), id, userId);
  }

  @Transactional
  public void activate(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Activating category {} for user: {}", id, userId);

    Category category = get(id);
    category.setArchivedAt(null);
    category.setArchivedBy(null);
    categoryRepo.save(category);

    log.info("Activated category '{}' with ID {} for user: {}", category.getName(), id, userId);
  }
}
