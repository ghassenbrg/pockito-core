package io.ghassen.pockito.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.ghassen.pockito.config.TestSecurityConfig;
import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.service.CategoryService;
import io.ghassen.pockito.web.dto.CategoryDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ContextConfiguration(classes = {CategoryController.class})
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CategoryService categoryService;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String USER_ID = "testuser";
  private static final UUID CATEGORY_ID = UUID.randomUUID();
  private static final String BASE_URL = "/api/categories";

  private Category testCategory;
  private CategoryDtos.CreateReq createReq;
  private CategoryDtos.UpdateReq updateReq;

  @BeforeEach
  void setUp() {
    testCategory = createTestCategory();
    createReq = createTestCreateReq();
    updateReq = createTestUpdateReq();
  }

  @Test
  @WithMockUser(roles = "USER")
  void list_ShouldReturnCategories_WhenAuthenticated() throws Exception {
    // Given
    when(categoryService.list(true)).thenReturn(List.of(testCategory));

    // When & Then
    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(testCategory.getId().toString()))
      .andExpect(jsonPath("$[0].name").value(testCategory.getName()))
      .andExpect(jsonPath("$[0].type").value(testCategory.getType().toString()));

    verify(categoryService).list(true);
  }

  @Test
  void list_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "USER")
  void getByType_ShouldReturnCategoriesByType_WhenAuthenticated() throws Exception {
    // Given
    when(categoryService.listByType(Category.CategoryType.EXPENSE, true))
        .thenReturn(List.of(testCategory));

    // When & Then
    mockMvc.perform(get(BASE_URL + "/type/EXPENSE"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(testCategory.getId().toString()))
      .andExpect(jsonPath("$[0].type").value("EXPENSE"));

    verify(categoryService).listByType(Category.CategoryType.EXPENSE, true);
  }

  @Test
  @WithMockUser(roles = "USER")
  void search_ShouldReturnMatchingCategories_WhenAuthenticated() throws Exception {
    // Given
    String searchTerm = "test";
    when(categoryService.searchCategories(searchTerm, true))
        .thenReturn(List.of(testCategory));

    // When & Then
    mockMvc.perform(get(BASE_URL + "/search")
        .param("searchTerm", searchTerm))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(testCategory.getId().toString()));

    verify(categoryService).searchCategories(searchTerm, true);
  }

  @Test
  @WithMockUser(roles = "USER")
  void getActiveCategories_ShouldReturnActiveCategoriesOnly_WhenAuthenticated() throws Exception {
    // Given
    when(categoryService.list(false)).thenReturn(List.of(testCategory));

    // When & Then
    mockMvc.perform(get(BASE_URL + "/status/active"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(testCategory.getId().toString()));

    verify(categoryService).list(false);
  }

  @Test
  @WithMockUser(roles = "USER")
  void get_ShouldReturnCategory_WhenExists() throws Exception {
    // Given
    when(categoryService.get(CATEGORY_ID)).thenReturn(testCategory);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{id}", CATEGORY_ID))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testCategory.getId().toString()))
      .andExpect(jsonPath("$.name").value(testCategory.getName()))
      .andExpect(jsonPath("$.type").value(testCategory.getType().toString()));

    verify(categoryService).get(CATEGORY_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void create_ShouldCreateCategory_WhenValidRequest() throws Exception {
    // Given
    when(categoryService.create(any(CategoryDtos.CreateReq.class))).thenReturn(testCategory);

    // When & Then
    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createReq)))
      .andExpect(status().isCreated())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testCategory.getId().toString()))
      .andExpect(jsonPath("$.name").value(testCategory.getName()));

    verify(categoryService).create(any(CategoryDtos.CreateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void create_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
    // Given
    CategoryDtos.CreateReq invalidReq = new CategoryDtos.CreateReq(
        "", null, "#invalid", null, null, null
    );

    // When & Then
    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidReq)))
      .andExpect(status().isBadRequest());

    verify(categoryService, never()).create(any());
  }

  @Test
  @WithMockUser(roles = "USER")
  void update_ShouldUpdateCategory_WhenValidRequest() throws Exception {
    // Given
    when(categoryService.update(eq(CATEGORY_ID), any(CategoryDtos.UpdateReq.class)))
        .thenReturn(testCategory);

    // When & Then
    mockMvc.perform(put(BASE_URL + "/{id}", CATEGORY_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateReq)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testCategory.getId().toString()))
      .andExpect(jsonPath("$.name").value(testCategory.getName()));

    verify(categoryService).update(eq(CATEGORY_ID), any(CategoryDtos.UpdateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void update_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
    // Given
    CategoryDtos.UpdateReq invalidReq = new CategoryDtos.UpdateReq(
        "", "#invalid", null, null, null
    );

    // When & Then
    mockMvc.perform(put(BASE_URL + "/{id}", CATEGORY_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidReq)))
      .andExpect(status().isBadRequest());

    verify(categoryService, never()).update(any(), any());
  }

  @Test
  @WithMockUser(roles = "USER")
  void archive_ShouldArchiveCategory_WhenValidRequest() throws Exception {
    // Given
    doNothing().when(categoryService).archive(CATEGORY_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/archive", CATEGORY_ID))
      .andExpect(status().isNoContent());

    verify(categoryService).archive(CATEGORY_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void activate_ShouldActivateCategory_WhenValidRequest() throws Exception {
    // Given
    doNothing().when(categoryService).activate(CATEGORY_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/activate", CATEGORY_ID))
      .andExpect(status().isNoContent());

    verify(categoryService).activate(CATEGORY_ID);
  }

  private Category createTestCategory() {
    return Category.builder()
        .id(CATEGORY_ID)
        .userId(USER_ID)
        .name("Test Category")
        .type(Category.CategoryType.EXPENSE)
        .color("#FF0000")
        .iconType(io.ghassen.pockito.domain.Wallet.IconType.EMOJI)
        .iconValue("💰")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();
  }

  private CategoryDtos.CreateReq createTestCreateReq() {
    return new CategoryDtos.CreateReq(
        "Test Category",
        Category.CategoryType.EXPENSE,
        "#FF0000",
        io.ghassen.pockito.domain.Wallet.IconType.EMOJI,
        "💰",
        null
    );
  }

  private CategoryDtos.UpdateReq createTestUpdateReq() {
    return new CategoryDtos.UpdateReq(
        "Updated Category", "#00FF00",
        io.ghassen.pockito.domain.Wallet.IconType.EMOJI,
        "💡",
        null
    );
  }
}
