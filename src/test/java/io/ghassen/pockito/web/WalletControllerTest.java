package io.ghassen.pockito.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ghassen.pockito.config.TestSecurityConfig;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.service.WalletService;
import io.ghassen.pockito.web.dto.WalletDtos;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@ContextConfiguration(classes = {WalletController.class})
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
class WalletControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WalletService walletService;

  @Autowired
  private ObjectMapper objectMapper;

  private static final String USER_ID = "123e4567-e89b-12d3-a456-426614174000";
  private static final UUID WALLET_ID = UUID.randomUUID();
  private static final String BASE_URL = "/api/wallets";

  private Wallet testWallet;
  private WalletDtos.CreateReq createReq;
  private WalletDtos.UpdateReq updateReq;

  @BeforeEach
  void setUp() {
    testWallet = createTestWallet();
    createReq = createTestCreateReq();
    updateReq = createTestUpdateReq();
  }

  @Test
  @WithMockUser(roles = "USER")
  void list_ShouldReturnWallets_WhenAuthenticated() throws Exception {
    // Given
    when(walletService.list(true)).thenReturn(List.of(testWallet));

    // When & Then
    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(testWallet.getId().toString()))
      .andExpect(jsonPath("$[0].name").value(testWallet.getName()))
      .andExpect(jsonPath("$[0].currencyCode").value(testWallet.getCurrencyCode()))
      .andExpect(jsonPath("$[0].isDefault").value(testWallet.isDefault()));

    verify(walletService).list(true);
  }

  @Test
  void list_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "USER")
  void get_ShouldReturnWallet_WhenExists() throws Exception {
    // Given
    when(walletService.get(WALLET_ID)).thenReturn(testWallet);

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{id}", WALLET_ID))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testWallet.getId().toString()))
      .andExpect(jsonPath("$.name").value(testWallet.getName()))
      .andExpect(jsonPath("$.currencyCode").value(testWallet.getCurrencyCode()));

    verify(walletService).get(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void get_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
    // Given
    when(walletService.get(WALLET_ID))
      .thenThrow(new jakarta.persistence.EntityNotFoundException("Wallet not found"));

    // When & Then
    mockMvc.perform(get(BASE_URL + "/{id}", WALLET_ID))
      .andExpect(status().isNotFound());

    verify(walletService).get(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void create_ShouldCreateWallet_WhenValidRequest() throws Exception {
    // Given
    when(walletService.create(any(WalletDtos.CreateReq.class))).thenReturn(testWallet);

    // When & Then
    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createReq)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", "/api/wallets/" + testWallet.getId()))
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testWallet.getId().toString()))
      .andExpect(jsonPath("$.name").value(testWallet.getName()));

    verify(walletService).create(any(WalletDtos.CreateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void create_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
    // Given
    WalletDtos.CreateReq invalidReq = new WalletDtos.CreateReq(
      "", // Empty name
      Wallet.IconType.EMOJI,
      "💰",
      "INVALID", // Invalid currency code
      "#INVALID", // Invalid color
      Wallet.WalletType.SAVINGS,
      BigDecimal.valueOf(-100), // Negative balance
      BigDecimal.valueOf(-1000), // Negative goal
      false
    );

    // When & Then
    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidReq)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.fields").exists());

    verify(walletService, never()).create(any(WalletDtos.CreateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void create_ShouldReturnBadRequest_WhenNameAlreadyExists() throws Exception {
    // Given
    when(walletService.create(any(WalletDtos.CreateReq.class)))
      .thenThrow(new IllegalArgumentException("Wallet name already exists"));

    // When & Then
    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createReq)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message").value("Wallet name already exists"));

    verify(walletService).create(any(WalletDtos.CreateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void update_ShouldUpdateWallet_WhenValidRequest() throws Exception {
    // Given
    when(walletService.update(eq(WALLET_ID), any(WalletDtos.UpdateReq.class)))
      .thenReturn(testWallet);

    // When & Then
    mockMvc.perform(put(BASE_URL + "/{id}", WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateReq)))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id").value(testWallet.getId().toString()))
      .andExpect(jsonPath("$.name").value(testWallet.getName()));

    verify(walletService).update(eq(WALLET_ID), any(WalletDtos.UpdateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void update_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
    // Given
    WalletDtos.UpdateReq invalidReq = new WalletDtos.UpdateReq(
      "", // Empty name
      Wallet.IconType.EMOJI,
      "💰",
      "INVALID", // Invalid currency code
      "#INVALID", // Invalid color
      Wallet.WalletType.SAVINGS,
      BigDecimal.valueOf(-1000) // Negative goal
    );

    // When & Then
    mockMvc.perform(put(BASE_URL + "/{id}", WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidReq)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.fields").exists());

    verify(walletService, never()).update(eq(WALLET_ID), any(WalletDtos.UpdateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void update_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
    // Given
    when(walletService.update(eq(WALLET_ID), any(WalletDtos.UpdateReq.class)))
      .thenThrow(new jakarta.persistence.EntityNotFoundException("Wallet not found"));

    // When & Then
    mockMvc.perform(put(BASE_URL + "/{id}", WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateReq)))
      .andExpect(status().isNotFound());

    verify(walletService).update(eq(WALLET_ID), any(WalletDtos.UpdateReq.class));
  }

  @Test
  @WithMockUser(roles = "USER")
  void archive_ShouldArchiveWallet_WhenValidRequest() throws Exception {
    // Given
    doNothing().when(walletService).archive(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/archive", WALLET_ID))
      .andExpect(status().isNoContent());

    verify(walletService).archive(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void archive_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
    // Given
    doThrow(new jakarta.persistence.EntityNotFoundException("Wallet not found"))
      .when(walletService).archive(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/archive", WALLET_ID))
      .andExpect(status().isNotFound());

    verify(walletService).archive(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void activate_ShouldActivateWallet_WhenValidRequest() throws Exception {
    // Given
    doNothing().when(walletService).activate(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/activate", WALLET_ID))
      .andExpect(status().isNoContent());

    verify(walletService).activate(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void activate_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
    // Given
    doThrow(new jakarta.persistence.EntityNotFoundException("Wallet not found"))
      .when(walletService).activate(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/activate", WALLET_ID))
      .andExpect(status().isNotFound());

    verify(walletService).activate(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void setDefault_ShouldSetWalletAsDefault_WhenValidRequest() throws Exception {
    // Given
    doNothing().when(walletService).setDefault(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/default", WALLET_ID))
      .andExpect(status().isNoContent());

    verify(walletService).setDefault(WALLET_ID);
  }

  @Test
  @WithMockUser(roles = "USER")
  void setDefault_ShouldReturnNotFound_WhenWalletDoesNotExist() throws Exception {
    // Given
    doThrow(new jakarta.persistence.EntityNotFoundException("Wallet not found"))
      .when(walletService).setDefault(WALLET_ID);

    // When & Then
    mockMvc.perform(post(BASE_URL + "/{id}/default", WALLET_ID))
      .andExpect(status().isNotFound());

    verify(walletService).setDefault(WALLET_ID);
  }

  @Test
  void allEndpoints_ShouldReturnForbidden_WhenNotAuthenticated() throws Exception {
    // When & Then
    mockMvc.perform(get(BASE_URL))
      .andExpect(status().isForbidden());

    mockMvc.perform(get(BASE_URL + "/{id}", WALLET_ID))
      .andExpect(status().isForbidden());

    mockMvc.perform(post(BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isForbidden());

    mockMvc.perform(put(BASE_URL + "/{id}", WALLET_ID)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isForbidden());

    mockMvc.perform(post(BASE_URL + "/{id}/archive", WALLET_ID))
      .andExpect(status().isForbidden());

    mockMvc.perform(post(BASE_URL + "/{id}/activate", WALLET_ID))
      .andExpect(status().isForbidden());

    mockMvc.perform(post(BASE_URL + "/{id}/default", WALLET_ID))
      .andExpect(status().isForbidden());
  }

  private Wallet createTestWallet() {
    Wallet wallet = Wallet.builder()
      .userId(USER_ID)
      .name("Test Wallet")
      .iconType(Wallet.IconType.EMOJI)
      .iconValue("💰")
      .currencyCode("USD")
      .color("#FF0000")
      .type(Wallet.WalletType.SAVINGS)
      .initialBalance(BigDecimal.valueOf(100))
      .isDefault(false)
      .goalAmount(BigDecimal.valueOf(1000))
      .build();
    
    wallet.setId(WALLET_ID);
    wallet.setCreatedAt(Instant.now());
    wallet.setUpdatedAt(Instant.now());
    return wallet;
  }

  private WalletDtos.CreateReq createTestCreateReq() {
    return new WalletDtos.CreateReq(
      "Test Wallet",
      Wallet.IconType.EMOJI,
      "💰",
      "USD",
      "#FF0000",
      Wallet.WalletType.SAVINGS,
      BigDecimal.valueOf(100),
      BigDecimal.valueOf(1000),
      false
    );
  }

  private WalletDtos.UpdateReq createTestUpdateReq() {
    return new WalletDtos.UpdateReq(
      "Updated Wallet",
      Wallet.IconType.EMOJI,
      "💰",
      "USD",
      "#FF0000",
      Wallet.WalletType.SAVINGS,
      BigDecimal.valueOf(1000)
    );
  }
}
