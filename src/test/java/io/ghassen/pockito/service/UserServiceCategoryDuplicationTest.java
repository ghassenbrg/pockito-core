package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.CategoryType;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService category duplication functionality.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceCategoryDuplicationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UserService userService;

    private User mockNewUser;
    private User mockSystemUser;
    private Category mockSystemCategory1;
    private Category mockSystemCategory2;
    private Category mockSystemChildCategory;

    @BeforeEach
    void setUp() {
        mockNewUser = User.builder()
                .username("newuser")
                .build();

        mockSystemUser = User.builder()
                .username("system")
                .build();

        // Create mock system categories
        mockSystemCategory1 = Category.builder()
                .id(UUID.randomUUID())
                .user(mockSystemUser)
                .name("Housing")
                .color("#5965F2")
                .categoryType(CategoryType.EXPENSE)
                .iconUrl("https://example.com/housing.svg")
                .parentCategory(null)
                .build();

        mockSystemCategory2 = Category.builder()
                .id(UUID.randomUUID())
                .user(mockSystemUser)
                .name("Income")
                .color("#27AE60")
                .categoryType(CategoryType.INCOME)
                .iconUrl("https://example.com/income.svg")
                .parentCategory(null)
                .build();

        mockSystemChildCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(mockSystemUser)
                .name("Rent")
                .color("#7B88FF")
                .categoryType(CategoryType.EXPENSE)
                .iconUrl("https://example.com/rent.svg")
                .parentCategory(mockSystemCategory1)
                .build();
    }

    @Test
    void getOrCreateUser_ShouldDuplicateSystemCategories_WhenCreatingNewUser() {
        // Given
        String username = "newuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockNewUser);
        
        List<Category> systemCategories = Arrays.asList(mockSystemCategory1, mockSystemCategory2, mockSystemChildCategory);
        when(categoryRepository.findHierarchicalCategoriesByUserUsername("system"))
                .thenReturn(systemCategories);
        
        // Mock category saves
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });
        
        when(categoryRepository.findById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            // Return a mock category with the requested ID
            Category category = Category.builder()
                    .id(id)
                    .user(mockNewUser)
                    .name("Mock Category")
                    .color("#000000")
                    .categoryType(CategoryType.EXPENSE)
                    .build();
            return Optional.of(category);
        });

        // When
        User result = userService.getOrCreateUser(username);

        // Then
        assertThat(result).isEqualTo(mockNewUser);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
        verify(categoryRepository, times(1)).findHierarchicalCategoriesByUserUsername("system");
        // Should save 3 categories (2 parents + 1 child)
        verify(categoryRepository, atLeast(3)).save(any(Category.class));
    }

    @Test
    void getOrCreateUser_ShouldNotDuplicateCategories_WhenUserAlreadyExists() {
        // Given
        String username = "existinguser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockNewUser));

        // When
        User result = userService.getOrCreateUser(username);

        // Then
        assertThat(result).isEqualTo(mockNewUser);
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
        verify(categoryRepository, never()).findHierarchicalCategoriesByUserUsername(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void duplicateSystemCategoriesForUser_ShouldHandleEmptySystemCategories() {
        // Given
        when(categoryRepository.findHierarchicalCategoriesByUserUsername("system"))
                .thenReturn(Arrays.asList());

        // When
        userService.duplicateSystemCategoriesForUser(mockNewUser);

        // Then
        verify(categoryRepository, times(1)).findHierarchicalCategoriesByUserUsername("system");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void duplicateSystemCategoriesForUser_ShouldCreateCategoriesWithCorrectProperties() {
        // Given
        List<Category> systemCategories = Arrays.asList(mockSystemCategory1, mockSystemCategory2);
        when(categoryRepository.findHierarchicalCategoriesByUserUsername("system"))
                .thenReturn(systemCategories);
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        // When
        userService.duplicateSystemCategoriesForUser(mockNewUser);

        // Then
        verify(categoryRepository, times(2)).save(argThat(category -> 
                category.getUser().equals(mockNewUser) &&
                category.isSystemAction() == true &&
                category.getParentCategory() == null
        ));
    }
}
