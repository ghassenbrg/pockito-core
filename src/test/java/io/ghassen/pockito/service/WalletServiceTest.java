package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.web.dto.WalletDtos;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock
  private WalletRepository walletRepo;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private JwtAuthenticationToken jwtAuth;

  @Mock
  private Jwt jwt;

  @InjectMocks
  private WalletService walletService;

  private static final String USER_ID = "123e4567-e89b-12d3-a456-426614174000";
  private static final UUID WALLET_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    when(securityContext.getAuthentication()).thenReturn(jwtAuth);
    when(jwtAuth.getToken()).thenReturn(jwt);
    when(jwt.getSubject()).thenReturn(USER_ID.toString());
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void list_ShouldReturnUserWallets() {
    // Given
    Wallet wallet1 = createTestWallet("Wallet 1");
    Wallet wallet2 = createTestWallet("Wallet 2");
    when(walletRepo.findByUserIdOrderByCreatedAtDesc(USER_ID))
      .thenReturn(List.of(wallet1, wallet2));

    // When
    List<Wallet> result = walletService.list(true);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(wallet1, wallet2);
    verify(walletRepo).findByUserIdOrderByCreatedAtDesc(USER_ID);
  }

  @Test
  void get_ShouldReturnWallet_WhenExists() {
    // Given
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(wallet));

    // When
    Wallet result = walletService.get(WALLET_ID);

    // Then
    assertThat(result).isEqualTo(wallet);
    verify(walletRepo).findByIdAndUserId(WALLET_ID, USER_ID);
  }

  @Test
  void get_ShouldThrowException_WhenWalletNotFound() {
    // Given
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> walletService.get(WALLET_ID))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessage("Wallet not found");
  }

  @Test
  void create_ShouldCreateWallet_WhenValidRequest() {
    // Given
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
      "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), false
    );
    
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, req.name()))
      .thenReturn(false);
    when(walletRepo.findByUserIdAndIsDefaultTrue(USER_ID))
      .thenReturn(Optional.empty());
    
    Wallet savedWallet = createTestWallet(req.name());
    when(walletRepo.save(any(Wallet.class))).thenReturn(savedWallet);

    // When
    Wallet result = walletService.create(req);

    // Then
    assertThat(result).isEqualTo(savedWallet);
    verify(walletRepo).existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, req.name());
    verify(walletRepo, times(1)).save(any(Wallet.class));
  }

  @Test
  void create_ShouldThrowException_WhenNameAlreadyExists() {
    // Given
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
      "Existing Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), false
    );
    
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, req.name()))
      .thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> walletService.create(req))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Wallet name already exists");
    
    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void create_ShouldThrowException_WhenSavingsGoalIsNegative() {
    // Given
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
      "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(-100), false
    );

    // When & Then
    assertThatThrownBy(() -> walletService.create(req))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Savings goal amount must be non-negative");
    
    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void create_ShouldSetAsDefault_WhenRequested() {
    // Given
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
      "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true
    );
    
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, req.name()))
      .thenReturn(false);
    
    Wallet savedWallet = createTestWallet(req.name());
    when(walletRepo.save(any(Wallet.class))).thenReturn(savedWallet);

    // When
    walletService.create(req);

    // Then
    verify(walletRepo, times(1)).save(any(Wallet.class)); // Only once since we set isDefault in builder
  }

  @Test
  void update_ShouldUpdateWallet_WhenValidRequest() {
    // Given
    Wallet existingWallet = createTestWallet("Old Name");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(existingWallet));
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, "New Name"))
      .thenReturn(false);
    
    WalletDtos.UpdateReq req = new WalletDtos.UpdateReq(
      "New Name", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(1000)
    );
    
    when(walletRepo.save(any(Wallet.class))).thenReturn(existingWallet);

    // When
    Wallet result = walletService.update(WALLET_ID, req);

    // Then
    assertThat(result).isEqualTo(existingWallet);
    assertThat(existingWallet.getName()).isEqualTo("New Name");
    verify(walletRepo).save(existingWallet);
  }

  @Test
  void update_ShouldThrowException_WhenNewNameAlreadyExists() {
    // Given
    Wallet existingWallet = createTestWallet("Old Name");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(existingWallet));
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, "New Name"))
      .thenReturn(true);
    
    WalletDtos.UpdateReq req = new WalletDtos.UpdateReq(
      "New Name", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
      Wallet.WalletType.SAVINGS, BigDecimal.valueOf(1000)
    );

    // When & Then
    assertThatThrownBy(() -> walletService.update(WALLET_ID, req))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Wallet name already exists");
    
    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void archive_ShouldArchiveWallet() {
    // Given
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(wallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    // When
    walletService.archive(WALLET_ID);

    // Then
    assertThat(wallet.getArchivedAt()).isNotNull();
    assertThat(wallet.getArchivedBy()).isEqualTo(USER_ID);
    verify(walletRepo).save(wallet);
  }

  @Test
  void archive_ShouldReassignDefault_WhenArchivingDefaultWallet() {
    // Given
    UUID defaultWalletId = UUID.randomUUID();
    UUID otherWalletId = UUID.randomUUID();
    
    Wallet defaultWallet = createTestWallet("Default Wallet", defaultWalletId);
    defaultWallet.setDefault(true);
    
    Wallet otherWallet = createTestWallet("Other Wallet", otherWalletId);
    otherWallet.setDefault(false);
    
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(defaultWallet));
    when(walletRepo.findByUserIdOrderByCreatedAtDesc(USER_ID))
      .thenReturn(List.of(defaultWallet, otherWallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(defaultWallet).thenReturn(otherWallet);

    // When
    walletService.archive(WALLET_ID);

    // Then
    verify(walletRepo, times(2)).save(any(Wallet.class)); // archive + reassign default
  }

  @Test
  void activate_ShouldActivateWallet() {
    // Given
    Wallet wallet = createTestWallet("Test Wallet");
    wallet.setArchivedAt(Instant.now());
    wallet.setArchivedBy(USER_ID.toString());
    
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(wallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    // When
    walletService.activate(WALLET_ID);

    // Then
    assertThat(wallet.getArchivedAt()).isNull();
    assertThat(wallet.getArchivedBy()).isNull();
    verify(walletRepo).save(wallet);
  }

  @Test
  void setDefault_ShouldSetWalletAsDefault() {
    // Given
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(wallet));
    when(walletRepo.findByUserIdAndIsDefaultTrue(USER_ID))
      .thenReturn(Optional.empty());
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    // When
    walletService.setDefault(WALLET_ID);

    // Then
    assertThat(wallet.isDefault()).isTrue();
    verify(walletRepo).save(wallet);
  }

  @Test
  void setDefault_ShouldUnsetPreviousDefault_WhenSettingNewDefault() {
    // Given
    UUID currentDefaultId = UUID.randomUUID();
    UUID newDefaultId = UUID.randomUUID();
    
    Wallet currentDefault = createTestWallet("Current Default", currentDefaultId);
    currentDefault.setDefault(true);
    
    Wallet newDefault = createTestWallet("New Default", newDefaultId);
    newDefault.setDefault(false);
    
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(newDefault));
    when(walletRepo.findByUserIdAndIsDefaultTrue(USER_ID))
      .thenReturn(Optional.of(currentDefault));
    when(walletRepo.save(any(Wallet.class))).thenReturn(currentDefault).thenReturn(newDefault);

    // When
    walletService.setDefault(WALLET_ID);

    // Then
    assertThat(currentDefault.isDefault()).isFalse();
    assertThat(newDefault.isDefault()).isTrue();
    verify(walletRepo, times(2)).save(any(Wallet.class));
  }

  @Test
  void setDefault_ShouldBeIdempotent_WhenSettingSameWalletAsDefault() {
    // Given
    Wallet wallet = createTestWallet("Test Wallet");
    wallet.setDefault(true);
    
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID))
      .thenReturn(Optional.of(wallet));
    when(walletRepo.findByUserIdAndIsDefaultTrue(USER_ID))
      .thenReturn(Optional.of(wallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    // When
    walletService.setDefault(WALLET_ID);

    // Then
    assertThat(wallet.isDefault()).isTrue();
    verify(walletRepo, times(1)).save(any(Wallet.class)); // Only one save for the same wallet
  }

  private Wallet createTestWallet(String name) {
    return createTestWallet(name, WALLET_ID);
  }

  private Wallet createTestWallet(String name, UUID id) {
    Wallet wallet = Wallet.builder()
      .userId(USER_ID)
      .name(name)
      .iconType(Wallet.IconType.EMOJI)
      .iconValue("💰")
      .currencyCode("USD")
      .color("#FF0000")
      .type(Wallet.WalletType.SAVINGS)
      .initialBalance(BigDecimal.valueOf(100))
      .isDefault(false)
      .goalAmount(BigDecimal.valueOf(1000))
      .build();
    
    // Set ID and timestamps manually since they're not part of the builder
    wallet.setId(id);
    wallet.setCreatedAt(Instant.now());
    wallet.setUpdatedAt(Instant.now());
    return wallet;
  }
}
