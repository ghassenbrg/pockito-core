package io.ghassen.pockito.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.dto.WalletDtos;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

  private final WalletRepository walletRepo;

  public List<Wallet> list(boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Listing wallets for user: {} with includeArchived: {}", userId, includeArchived);
    
    if (includeArchived) {
      return walletRepo.findByUserIdOrderByDisplayOrder(userId);
    } else {
      return walletRepo.findActiveByUserId(userId);
    }
  }

  public List<Wallet> searchWallets(String searchTerm, boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Searching wallets for user: {} with term: '{}', includeArchived: {}", userId, searchTerm, includeArchived);
    
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return list(includeArchived);
    }
    
    if (includeArchived) {
      return walletRepo.searchByUserIdAndTerm(userId, searchTerm.trim());
    } else {
      return walletRepo.searchActiveByUserIdAndTerm(userId, searchTerm.trim());
    }
  }

  public List<Wallet> getWalletsByType(Wallet.WalletType type, boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Getting wallets by type: {} for user: {} with includeArchived: {}", type, userId, includeArchived);
    
    if (includeArchived) {
      return walletRepo.findByUserIdAndType(userId, type);
    } else {
      return walletRepo.findActiveByUserIdAndType(userId, type);
    }
  }

  public List<Wallet> getWalletsByCurrency(String currencyCode, boolean includeArchived) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Getting wallets by currency: {} for user: {} with includeArchived: {}", currencyCode, userId, includeArchived);
    
    if (includeArchived) {
      return walletRepo.findByUserIdAndCurrencyCode(userId, currencyCode);
    } else {
      return walletRepo.findActiveByUserIdAndCurrencyCode(userId, currencyCode);
    }
  }

  public Wallet get(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Getting wallet {} for user: {}", id, userId);
    return walletRepo.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
  }

  @Transactional
  public Wallet create(WalletDtos.CreateReq req) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Creating wallet '{}' for user: {}", req.name(), userId);

    if (walletRepo.existsActiveByNameIgnoreCase(userId, req.name())) {
      throw new IllegalArgumentException("Wallet name already exists");
    }

    // Get the next available display order
    Integer nextDisplayOrder = walletRepo.findMaxDisplayOrderByUserId(userId);
    nextDisplayOrder = (nextDisplayOrder != null ? nextDisplayOrder : 0) + 1;

    Wallet wallet = Wallet.builder()
        .userId(userId)
        .name(req.name())
        .iconType(req.iconType())
        .iconValue(req.iconValue())
        .currencyCode(req.currencyCode())
        .color(req.color())
        .type(req.type())
        .initialBalance(req.initialBalance() != null ? req.initialBalance() : BigDecimal.ZERO)
        .goalAmount(req.goalAmount())
        .isDefault(false)
        .displayOrder(nextDisplayOrder)
        .build();

    wallet = walletRepo.save(wallet);
    log.info("Created wallet '{}' with ID {} for user: {}", wallet.getName(), wallet.getId(), userId);

    if (req.setDefault() || walletRepo.findByUserIdAndIsDefaultTrue(userId).isEmpty()) {
      setDefault(wallet.getId());
    }

    return wallet;
  }

  @Transactional
  public Wallet update(UUID id, WalletDtos.UpdateReq req) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Updating wallet {} for user: {}", id, userId);

    Wallet wallet = get(id);

    // Check for unique name if changed
    if (!wallet.getName().equalsIgnoreCase(req.name()) &&
        walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(userId, req.name())) {
      throw new IllegalArgumentException("Wallet name already exists");
    }

    // Validate goal amount for savings
    if (req.type() == Wallet.WalletType.SAVINGS && req.goalAmount() != null
        && req.goalAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Savings goal amount must be non-negative");
    }

    wallet.setName(req.name());
    wallet.setIconType(req.iconType());
    wallet.setIconValue(req.iconValue());
    wallet.setCurrencyCode(req.currencyCode());
    wallet.setColor(req.color());
    wallet.setType(req.type());
    wallet.setGoalAmount(req.goalAmount());

    wallet = walletRepo.save(wallet);
    log.info("Updated wallet '{}' with ID {} for user: {}", req.name(), id, userId);
    return wallet;
  }

  @Transactional
  public void archive(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Archiving wallet {} for user: {}", id, userId);

    Wallet wallet = get(id);
    if (wallet.getArchivedAt() != null) {
      throw new IllegalArgumentException("Wallet is already archived");
    }

    // If archiving the default wallet, find another wallet to reassign default to
    Wallet nextDefault = null;
    if (wallet.isDefault()) {
      List<Wallet> otherWallets = walletRepo.findByUserIdOrderByDisplayOrder(userId).stream()
          .filter(other -> other.getArchivedAt() == null && !other.getId().equals(wallet.getId()))
          .toList();

      if (!otherWallets.isEmpty()) {
        nextDefault = otherWallets.get(0);
        log.info("Will reassign default from wallet '{}' to '{}' for user: {}",
            wallet.getName(), nextDefault.getName(), userId);
      }
    }

    // Archive the wallet
    wallet.setDefault(false);
    wallet.setArchivedAt(Instant.now());
    wallet.setArchivedBy(userId);
    walletRepo.save(wallet);

    // Reassign default if needed
    if (nextDefault != null) {
      nextDefault.setDefault(true);
      walletRepo.save(nextDefault);
      log.info("Reassigned default to wallet '{}' for user: {}", nextDefault.getName(), userId);
    }

    // Normalize display orders after archiving to ensure proper sequencing
    normalizeDisplayOrders();

    log.info("Archived wallet '{}' with ID {} for user: {}", wallet.getName(), id, userId);
  }

  @Transactional
  public void activate(UUID id) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Activating wallet {} for user: {}", id, userId);

    Wallet wallet = get(id);
    wallet.setArchivedAt(null);
    wallet.setArchivedBy(null);
    walletRepo.save(wallet);

    // Normalize display orders after activation to ensure proper sequencing
    normalizeDisplayOrders();

    log.info("Activated wallet '{}' with ID {} for user: {}", wallet.getName(), id, userId);
  }

  @Transactional
  public void setDefault(UUID id) {
    final String userId = SecurityUtils.getCurrentUserId();
    log.debug("Setting wallet {} as default for user: {}", id, userId);

    // Get the wallet and check if it's archived
    Wallet wallet = get(id);
    if (wallet.getArchivedAt() != null) {
      throw new IllegalArgumentException("Cannot set archived wallet as default");
    }

    // lock the current default row to serialize concurrent calls
    walletRepo.findDefaultForUpdate(userId);

    int updated = walletRepo.setDefaultForUser(userId, id);
    if (updated == 0) {
      throw new IllegalArgumentException("Wallet not found for current user or no rows updated.");
    }
    
    log.info("Set wallet '{}' as default for user: {}", wallet.getName(), userId);
  }

  @Transactional
  public void reorderWallets(UUID walletId, Integer newOrder) {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Reordering wallet {} to position {} for user: {}", walletId, newOrder, userId);

    Wallet wallet = get(walletId);
    if (wallet.getArchivedAt() != null) {
      throw new IllegalArgumentException("Cannot reorder archived wallet");
    }

    // Get all active wallets for the user, ordered by current display order
    List<Wallet> activeWallets = walletRepo.findActiveByUserId(userId);
    if (activeWallets.isEmpty()) {
      log.warn("No active wallets found for user: {}", userId);
      return;
    }

    // Validate the new order is within bounds
    if (newOrder < 1 || newOrder > activeWallets.size()) {
      throw new IllegalArgumentException("New order must be between 1 and " + activeWallets.size());
    }

    Integer currentOrder = wallet.getDisplayOrder();
    if (currentOrder.equals(newOrder)) {
      log.debug("Wallet {} is already at position {}, no reordering needed", walletId, newOrder);
      return;
    }

    // Create a new list with the reordered wallet
    List<Wallet> reorderedWallets = new ArrayList<>(activeWallets);
    
    // Remove the wallet from its current position
    reorderedWallets.removeIf(w -> w.getId().equals(walletId));
    
    // Insert the wallet at the new position (adjust for 0-based index)
    reorderedWallets.add(newOrder - 1, wallet);
    
    // Reassign display orders sequentially starting from 1
    for (int i = 0; i < reorderedWallets.size(); i++) {
      Wallet w = reorderedWallets.get(i);
      w.setDisplayOrder(i + 1);
      walletRepo.save(w);
    }

    log.info("Reordered wallet '{}' from position {} to {} for user: {}. New order: {}", 
        wallet.getName(), currentOrder, newOrder, userId, 
        reorderedWallets.stream().map(Wallet::getDisplayOrder).toList());
  }

  /**
   * Normalize display orders to ensure they are sequential starting from 1.
   * This method fixes any gaps or inconsistencies in the display order.
   */
  @Transactional
  public void normalizeDisplayOrders() {
    String userId = SecurityUtils.getCurrentUserId();
    log.debug("Normalizing display orders for user: {}", userId);

    List<Wallet> activeWallets = walletRepo.findActiveByUserId(userId);
    if (activeWallets.isEmpty()) {
      log.debug("No active wallets found for user: {}", userId);
      return;
    }

    // Sort by current display order to maintain relative positioning
    activeWallets.sort((w1, w2) -> {
      Integer order1 = w1.getDisplayOrder();
      Integer order2 = w2.getDisplayOrder();
      if (order1 == null) order1 = Integer.MAX_VALUE;
      if (order2 == null) order1 = Integer.MAX_VALUE;
      return order1.compareTo(order2);
    });

    // Reassign display orders sequentially starting from 1
    boolean hasChanges = false;
    for (int i = 0; i < activeWallets.size(); i++) {
      Wallet wallet = activeWallets.get(i);
      Integer expectedOrder = i + 1;
      
      if (!expectedOrder.equals(wallet.getDisplayOrder())) {
        wallet.setDisplayOrder(expectedOrder);
        walletRepo.save(wallet);
        hasChanges = true;
        log.debug("Fixed display order for wallet '{}': {} -> {}", 
            wallet.getName(), wallet.getDisplayOrder(), expectedOrder);
      }
    }

    if (hasChanges) {
      log.info("Normalized display orders for {} wallets for user: {}", activeWallets.size(), userId);
    } else {
      log.debug("Display orders are already normalized for user: {}", userId);
    }
  }
}
