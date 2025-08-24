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

  @Mock private WalletRepository walletRepo;
  @Mock private SecurityContext securityContext;
  @Mock private JwtAuthenticationToken jwtAuth;
  @Mock private Jwt jwt;

  @InjectMocks private WalletService walletService;

  private static final String USER_ID = "123e4567-e89b-12d3-a456-426614174000";
  private static final UUID WALLET_ID = UUID.fromString("d452e473-9c7a-4c18-a0cb-163bb6f30edb");

  @BeforeEach
  void setUp() {
    when(securityContext.getAuthentication()).thenReturn(jwtAuth);
    when(jwtAuth.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsString("preferred_username")).thenReturn(USER_ID);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void list_ShouldReturnUserWallets() {
    Wallet wallet1 = createTestWallet("Wallet 1");
    Wallet wallet2 = createTestWallet("Wallet 2");
    when(walletRepo.findByUserIdOrderByDisplayOrder(USER_ID))
        .thenReturn(List.of(wallet1, wallet2));

    List<Wallet> result = walletService.list(true);

    assertThat(result).hasSize(2).containsExactly(wallet1, wallet2);
    verify(walletRepo).findByUserIdOrderByDisplayOrder(USER_ID);
  }

  @Test
  void get_ShouldReturnWallet_WhenExists() {
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(wallet));

    Wallet result = walletService.get(WALLET_ID);

    assertThat(result).isEqualTo(wallet);
    verify(walletRepo).findByIdAndUserId(WALLET_ID, USER_ID);
  }

  @Test
  void get_ShouldThrowException_WhenWalletNotFound() {
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> walletService.get(WALLET_ID))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Wallet not found");
  }

  @Test
  void create_ShouldCreateWallet_WhenValidRequest_AndAutoSetDefaultIfNone() {
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
        "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), false
    );

    when(walletRepo.existsActiveByNameIgnoreCase(USER_ID, req.name())).thenReturn(false);
    when(walletRepo.findMaxDisplayOrderByUserId(USER_ID)).thenReturn(0);
    // No current default -> service should set newly created wallet as default
    when(walletRepo.findByUserIdAndIsDefaultTrue(USER_ID)).thenReturn(Optional.empty());

    Wallet savedWallet = createTestWallet(req.name());
    when(walletRepo.save(any(Wallet.class))).thenReturn(savedWallet);

    // setDefault(...) path
    when(walletRepo.findByIdAndUserId(savedWallet.getId(), USER_ID)).thenReturn(Optional.of(savedWallet));
    when(walletRepo.findDefaultForUpdate(USER_ID)).thenReturn(Optional.empty());
    when(walletRepo.setDefaultForUser(USER_ID, savedWallet.getId())).thenReturn(1);

    Wallet result = walletService.create(req);

    assertThat(result).isEqualTo(savedWallet);
    verify(walletRepo).existsActiveByNameIgnoreCase(USER_ID, req.name());
    verify(walletRepo).findMaxDisplayOrderByUserId(USER_ID);
    verify(walletRepo).save(any(Wallet.class));
    verify(walletRepo).setDefaultForUser(USER_ID, savedWallet.getId());
  }

  @Test
  void create_ShouldThrowException_WhenNameAlreadyExists() {
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
        "Existing Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), false
    );

    when(walletRepo.existsActiveByNameIgnoreCase(USER_ID, req.name())).thenReturn(true);

    assertThatThrownBy(() -> walletService.create(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Wallet name already exists");

    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void create_ShouldThrowException_WhenSavingsGoalIsNegative() {
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
        "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(-100), false
    );

    when(walletRepo.existsActiveByNameIgnoreCase(USER_ID, req.name())).thenReturn(false);

    assertThatThrownBy(() -> walletService.create(req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Savings goal amount must be non-negative");

    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void create_ShouldSetAsDefault_WhenRequested() {
    WalletDtos.CreateReq req = new WalletDtos.CreateReq(
        "Test Wallet", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true
    );

    when(walletRepo.existsActiveByNameIgnoreCase(USER_ID, req.name())).thenReturn(false);
    when(walletRepo.findMaxDisplayOrderByUserId(USER_ID)).thenReturn(0);
    // DO NOT stub findByUserIdAndIsDefaultTrue() here; it won't be called because setDefault==true

    Wallet savedWallet = createTestWallet(req.name());
    when(walletRepo.save(any(Wallet.class))).thenReturn(savedWallet);

    // setDefault(...) path
    when(walletRepo.findByIdAndUserId(savedWallet.getId(), USER_ID)).thenReturn(Optional.of(savedWallet));
    when(walletRepo.findDefaultForUpdate(USER_ID)).thenReturn(Optional.empty());
    when(walletRepo.setDefaultForUser(USER_ID, savedWallet.getId())).thenReturn(1);

    walletService.create(req);

    verify(walletRepo).existsActiveByNameIgnoreCase(USER_ID, req.name());
    verify(walletRepo).findMaxDisplayOrderByUserId(USER_ID);
    verify(walletRepo).save(any(Wallet.class));
    verify(walletRepo).setDefaultForUser(USER_ID, savedWallet.getId());
    verify(walletRepo, never()).findByUserIdAndIsDefaultTrue(USER_ID);
  }

  @Test
  void update_ShouldUpdateWallet_WhenValidRequest() {
    Wallet existingWallet = createTestWallet("Old Name");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(existingWallet));
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, "New Name")).thenReturn(false);

    WalletDtos.UpdateReq req = new WalletDtos.UpdateReq(
        "New Name", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(1000)
    );

    when(walletRepo.save(any(Wallet.class))).thenReturn(existingWallet);

    Wallet result = walletService.update(WALLET_ID, req);

    assertThat(result).isEqualTo(existingWallet);
    assertThat(existingWallet.getName()).isEqualTo("New Name");
    verify(walletRepo).save(existingWallet);
  }

  @Test
  void update_ShouldThrowException_WhenNewNameAlreadyExists() {
    Wallet existingWallet = createTestWallet("Old Name");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(existingWallet));
    when(walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(USER_ID, "New Name")).thenReturn(true);

    WalletDtos.UpdateReq req = new WalletDtos.UpdateReq(
        "New Name", Wallet.IconType.EMOJI, "💰", "USD", "#FF0000",
        Wallet.WalletType.SAVINGS, BigDecimal.valueOf(1000)
    );

    assertThatThrownBy(() -> walletService.update(WALLET_ID, req))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Wallet name already exists");

    verify(walletRepo, never()).save(any(Wallet.class));
  }

  @Test
  void archive_ShouldArchiveWallet() {
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(wallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    walletService.archive(WALLET_ID);

    assertThat(wallet.getArchivedAt()).isNotNull();
    assertThat(wallet.getArchivedBy()).isEqualTo(USER_ID);
    verify(walletRepo).save(wallet);
  }

  @Test
  void archive_ShouldReassignDefault_WhenArchivingDefaultWallet() {
    UUID defaultWalletId = UUID.randomUUID();
    UUID otherWalletId = UUID.randomUUID();

    Wallet defaultWallet = createTestWallet("Default Wallet", defaultWalletId);
    defaultWallet.setDefault(true);

    Wallet otherWallet = createTestWallet("Other Wallet", otherWalletId);
    otherWallet.setDefault(false);

    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(defaultWallet));
    when(walletRepo.findByUserIdOrderByDisplayOrder(USER_ID)).thenReturn(List.of(defaultWallet, otherWallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(defaultWallet).thenReturn(otherWallet);

    walletService.archive(WALLET_ID);

    verify(walletRepo, times(2)).save(any(Wallet.class)); // archive + reassign default
  }

  @Test
  void activate_ShouldActivateWallet() {
    Wallet wallet = createTestWallet("Test Wallet");
    wallet.setArchivedAt(Instant.now());
    wallet.setArchivedBy(USER_ID);

    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(wallet));
    when(walletRepo.save(any(Wallet.class))).thenReturn(wallet);

    walletService.activate(WALLET_ID);

    assertThat(wallet.getArchivedAt()).isNull();
    assertThat(wallet.getArchivedBy()).isNull();
    verify(walletRepo).save(wallet);
  }

  @Test
  void setDefault_ShouldSetWalletAsDefault() {
    Wallet wallet = createTestWallet("Test Wallet");
    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(wallet));
    when(walletRepo.findDefaultForUpdate(USER_ID)).thenReturn(Optional.empty());
    when(walletRepo.setDefaultForUser(USER_ID, WALLET_ID)).thenReturn(1);

    walletService.setDefault(WALLET_ID);

    verify(walletRepo).findByIdAndUserId(WALLET_ID, USER_ID);
    verify(walletRepo).findDefaultForUpdate(USER_ID);
    verify(walletRepo).setDefaultForUser(USER_ID, WALLET_ID);
  }

  @Test
  void setDefault_ShouldUnsetPreviousDefault_WhenSettingNewDefault() {
    UUID currentDefaultId = UUID.randomUUID();
    UUID newDefaultId = UUID.randomUUID();

    Wallet currentDefault = createTestWallet("Current Default", currentDefaultId);
    currentDefault.setDefault(true);

    Wallet newDefault = createTestWallet("New Default", newDefaultId);
    newDefault.setDefault(false);

    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(newDefault));
    when(walletRepo.findDefaultForUpdate(USER_ID)).thenReturn(Optional.of(currentDefault));
    when(walletRepo.setDefaultForUser(USER_ID, WALLET_ID)).thenReturn(1);

    walletService.setDefault(WALLET_ID);

    verify(walletRepo).findByIdAndUserId(WALLET_ID, USER_ID);
    verify(walletRepo).findDefaultForUpdate(USER_ID);
    verify(walletRepo).setDefaultForUser(USER_ID, WALLET_ID);
  }

  @Test
  void setDefault_ShouldBeIdempotent_WhenSettingSameWalletAsDefault() {
    Wallet wallet = createTestWallet("Test Wallet");
    wallet.setDefault(true);

    when(walletRepo.findByIdAndUserId(WALLET_ID, USER_ID)).thenReturn(Optional.of(wallet));
    when(walletRepo.findDefaultForUpdate(USER_ID)).thenReturn(Optional.of(wallet));
    when(walletRepo.setDefaultForUser(USER_ID, WALLET_ID)).thenReturn(1);

    walletService.setDefault(WALLET_ID);

    verify(walletRepo).findByIdAndUserId(WALLET_ID, USER_ID);
    verify(walletRepo).findDefaultForUpdate(USER_ID);
    verify(walletRepo).setDefaultForUser(USER_ID, WALLET_ID);
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

    wallet.setId(id);
    wallet.setCreatedAt(Instant.now());
    wallet.setUpdatedAt(Instant.now());
    return wallet;
  }
}
