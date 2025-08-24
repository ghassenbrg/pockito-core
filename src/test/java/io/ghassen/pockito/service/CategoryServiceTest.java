package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.web.dto.CategoryDtos;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepo;
  @Mock private SecurityContext securityContext;
  @Mock private JwtAuthenticationToken jwtAuth;
  @Mock private Jwt jwt;

  @InjectMocks private CategoryService categoryService;

  private static final String USER_ID = "testuser";
  private static final UUID CATEGORY_ID = UUID.fromString("d452e473-9c7a-4c18-a0cb-163bb6f30edb");
  private static final UUID PARENT_CATEGORY_ID = UUID.fromString("e452e473-9c7a-4c18-a0cb-163bb6f30edb");

  @BeforeEach
  void setUp() {
    when(securityContext.getAuthentication()).thenReturn(jwtAuth);
    when(jwtAuth.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsString("preferred_username")).thenReturn(USER_ID);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void list_ShouldReturnUserCategories() {
    Category category1 = createTestCategory("Category 1", Category.CategoryType.EXPENSE);
    Category category2 = createTestCategory("Category 2", Category.CategoryType.INCOME);
    when(categoryRepo.findByUserIdOrderByNameAsc(USER_ID))
        .thenReturn(List.of(category1, category2));

    var result = categoryService.list(true);

    assertThat(result).hasSize(2).containsExactly(category1, category2);
    verify(categoryRepo).findByUserIdOrderByNameAsc(USER_ID);
  }

  @Test
  void list_ShouldReturnActiveCategoriesOnly() {
    Category activeCategory = createTestCategory("Active Category", Category.CategoryType.EXPENSE);
    when(categoryRepo.findActiveByUserId(USER_ID))
        .thenReturn(List.of(activeCategory));

    var result = categoryService.list(false);

    assertThat(result).hasSize(1).containsExactly(activeCategory);
    verify(categoryRepo).findActiveByUserId(USER_ID);
  }

  @Test
  void listByType_ShouldReturnCategoriesByType() {
    Category expenseCategory = createTestCategory("Expense Category", Category.CategoryType.EXPENSE);
    when(categoryRepo.findByUserIdAndType(USER_ID, Category.CategoryType.EXPENSE))
        .thenReturn(List.of(expenseCategory));

    var result = categoryService.listByType(Category.CategoryType.EXPENSE, true);

    assertThat(result).hasSize(1).containsExactly(expenseCategory);
    verify(categoryRepo).findByUserIdAndType(USER_ID, Category.CategoryType.EXPENSE);
  }

  @Test
  void listByType_ShouldReturnActiveCategoriesByType() {
    Category activeExpenseCategory = createTestCategory("Active Expense", Category.CategoryType.EXPENSE);
    when(categoryRepo.findActiveByUserIdAndType(USER_ID, Category.CategoryType.EXPENSE))
        .thenReturn(List.of(activeExpenseCategory));

    var result = categoryService.listByType(Category.CategoryType.EXPENSE, false);

    assertThat(result).hasSize(1).containsExactly(activeExpenseCategory);
    verify(categoryRepo).findActiveByUserIdAndType(USER_ID, Category.CategoryType.EXPENSE);
  }

  @Test
  void searchCategories_ShouldReturnMatchingCategories() {
    Category matchingCategory = createTestCategory("Matching Category", Category.CategoryType.EXPENSE);
    when(categoryRepo.searchByUserIdAndTerm(USER_ID, "Matching"))
        .thenReturn(List.of(matchingCategory));

    var result = categoryService.searchCategories("Matching", true);

    assertThat(result).hasSize(1).containsExactly(matchingCategory);
    verify(categoryRepo).searchByUserIdAndTerm(USER_ID, "Matching");
  }

  @Test
  void searchCategories_ShouldReturnAllCategories_WhenSearchTermIsEmpty() {
    Category category1 = createTestCategory("Category 1", Category.CategoryType.EXPENSE);
    Category category2 = createTestCategory("Category 2", Category.CategoryType.INCOME);
    when(categoryRepo.findByUserIdOrderByNameAsc(USER_ID))
        .thenReturn(List.of(category1, category2));

    var result = categoryService.searchCategories("", true);

    assertThat(result).hasSize(2).containsExactly(category1, category2);
    verify(categoryRepo).findByUserIdOrderByNameAsc(USER_ID);
  }

  @Test
  void get_ShouldReturnCategory_WhenExists() {
    Category category = createTestCategory("Test Category", Category.CategoryType.EXPENSE);
    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.of(category));

    var result = categoryService.get(CATEGORY_ID);

    assertThat(result).isEqualTo(category);
    verify(categoryRepo).findByIdAndUserId(CATEGORY_ID, USER_ID);
  }

  @Test
  void get_ShouldThrowException_WhenCategoryNotFound() {
    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.get(CATEGORY_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Category not found");
  }

  @Test
  void create_ShouldCreateCategory_WhenValidRequest() {
    var req = new CategoryDtos.CreateReq(
        "Test Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", null
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(false);
    when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = categoryService.create(req);

    assertThat(result.getName()).isEqualTo(req.name());
    assertThat(result.getType()).isEqualTo(req.type());
    assertThat(result.getUserId()).isEqualTo(USER_ID);
    verify(categoryRepo).save(any(Category.class));
  }

  @Test
  void create_ShouldThrowException_WhenNameAlreadyExists() {
    var req = new CategoryDtos.CreateReq(
        "Existing Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", null
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(true);

    assertThatThrownBy(() -> categoryService.create(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Category name already exists for this type");
  }

  @Test
  void create_ShouldCreateCategoryWithParent_WhenValidParentProvided() {
    Category parentCategory = createTestCategory("Parent Category", Category.CategoryType.EXPENSE);
    var req = new CategoryDtos.CreateReq(
        "Child Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", PARENT_CATEGORY_ID
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(false);
    when(categoryRepo.findByIdAndUserId(PARENT_CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(parentCategory));
    when(categoryRepo.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = categoryService.create(req);

    assertThat(result.getParent()).isNotNull();
    assertThat(result.getParent().getId()).isEqualTo(PARENT_CATEGORY_ID);
    verify(categoryRepo).save(any(Category.class));
  }

  @Test
  void create_ShouldThrowException_WhenParentNotFound() {
    var req = new CategoryDtos.CreateReq(
        "Child Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", PARENT_CATEGORY_ID
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(false);
    when(categoryRepo.findByIdAndUserId(PARENT_CATEGORY_ID, USER_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.create(req))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Parent category not found");
  }

  @Test
  void create_ShouldThrowException_WhenParentIsArchived() {
    Category archivedParent = createTestCategory("Archived Parent", Category.CategoryType.EXPENSE);
    archivedParent.setArchivedAt(Instant.now());

    var req = new CategoryDtos.CreateReq(
        "Child Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", PARENT_CATEGORY_ID
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(false);
    when(categoryRepo.findByIdAndUserId(PARENT_CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(archivedParent));

    assertThatThrownBy(() -> categoryService.create(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot create category under archived parent");
  }

  @Test
  void create_ShouldThrowException_WhenParentTypeMismatch() {
    Category incomeParent = createTestCategory("Income Parent", Category.CategoryType.INCOME);
    var req = new CategoryDtos.CreateReq(
        "Expense Category", Category.CategoryType.EXPENSE, "#FF0000",
        Wallet.IconType.EMOJI, "💰", PARENT_CATEGORY_ID
    );

    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCase(USER_ID, req.type(), req.name()))
        .thenReturn(false);
    when(categoryRepo.findByIdAndUserId(PARENT_CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(incomeParent));

    assertThatThrownBy(() -> categoryService.create(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Parent category must be of the same type");
  }

  @Test
  void update_ShouldUpdateCategory_WhenValidRequest() {
    Category existingCategory = createTestCategory("Old Name", Category.CategoryType.EXPENSE);
    var req = new CategoryDtos.UpdateReq(
        "New Name", Category.CategoryType.EXPENSE, "#00FF00",
        Wallet.IconType.EMOJI, "💡", null
    );

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(existingCategory));
    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCaseExcludingId(
        USER_ID, existingCategory.getType(), req.name(), CATEGORY_ID))
        .thenReturn(false);
    when(categoryRepo.save(any(Category.class))).thenReturn(existingCategory);

    var result = categoryService.update(CATEGORY_ID, req);

    assertThat(result.getName()).isEqualTo(req.name());
    assertThat(result.getColor()).isEqualTo(req.color());
    verify(categoryRepo).save(existingCategory);
  }

  @Test
  void update_ShouldThrowException_WhenNameAlreadyExists() {
    Category existingCategory = createTestCategory("Old Name", Category.CategoryType.EXPENSE);
    var req = new CategoryDtos.UpdateReq(
        "Existing Name", Category.CategoryType.EXPENSE, "#00FF00",
        Wallet.IconType.EMOJI, "💡", null
    );

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(existingCategory));
    when(categoryRepo.existsActiveByUserIdAndTypeAndNameIgnoreCaseExcludingId(
        USER_ID, existingCategory.getType(), req.name(), CATEGORY_ID))
        .thenReturn(true);

    assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Category name already exists for this type");
  }

  @Test
  void update_ShouldThrowException_WhenCircularReference() {
    Category existingCategory = createTestCategory("Test Category", Category.CategoryType.EXPENSE);
    var req = new CategoryDtos.UpdateReq(
        "Test Category", Category.CategoryType.EXPENSE, "#00FF00",
        Wallet.IconType.EMOJI, "💡", CATEGORY_ID
    );

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(existingCategory));

    assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Category cannot be its own parent");
  }

  @Test
  void archive_ShouldArchiveCategory_WhenValidRequest() {
    Category category = createTestCategory("Test Category", Category.CategoryType.EXPENSE);
    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(category));
    when(categoryRepo.findActiveByParentId(CATEGORY_ID))
        .thenReturn(List.of());
    when(categoryRepo.save(any(Category.class))).thenReturn(category);

    categoryService.archive(CATEGORY_ID);

    assertThat(category.getArchivedAt()).isNotNull();
    assertThat(category.getArchivedBy()).isEqualTo(USER_ID);
    verify(categoryRepo).save(category);
  }

  @Test
  void archive_ShouldThrowException_WhenAlreadyArchived() {
    Category archivedCategory = createTestCategory("Archived Category", Category.CategoryType.EXPENSE);
    archivedCategory.setArchivedAt(Instant.now());

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(archivedCategory));

    assertThatThrownBy(() -> categoryService.archive(CATEGORY_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Category is already archived");
  }

  @Test
  void archive_ShouldThrowException_WhenHasActiveChildren() {
    Category category = createTestCategory("Parent Category", Category.CategoryType.EXPENSE);
    Category childCategory = createTestCategory("Child Category", Category.CategoryType.EXPENSE);

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(category));
    when(categoryRepo.findActiveByParentId(CATEGORY_ID))
        .thenReturn(List.of(childCategory));

    assertThatThrownBy(() -> categoryService.archive(CATEGORY_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot archive category with active children. Please archive or move the children first.");
  }

  @Test
  void activate_ShouldActivateCategory_WhenValidRequest() {
    Category archivedCategory = createTestCategory("Archived Category", Category.CategoryType.EXPENSE);
    archivedCategory.setArchivedAt(Instant.now());
    archivedCategory.setArchivedBy(USER_ID);

    when(categoryRepo.findByIdAndUserId(CATEGORY_ID, USER_ID))
        .thenReturn(Optional.of(archivedCategory));
    when(categoryRepo.save(any(Category.class))).thenReturn(archivedCategory);

    categoryService.activate(CATEGORY_ID);

    assertThat(archivedCategory.getArchivedAt()).isNull();
    assertThat(archivedCategory.getArchivedBy()).isNull();
    verify(categoryRepo).save(archivedCategory);
  }

  private Category createTestCategory(String name, Category.CategoryType type) {
    return Category.builder()
        .id(CATEGORY_ID)
        .userId(USER_ID)
        .name(name)
        .type(type)
        .color("#FF0000")
        .iconType(Wallet.IconType.EMOJI)
        .iconValue("💰")
        .build();
  }
}
