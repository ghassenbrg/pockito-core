package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.web.dto.WalletDtos;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
  
  private final WalletRepository walletRepo;

  private UUID currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwt) {
      String subject = jwt.getToken().getSubject();
      try {
        return UUID.fromString(subject);
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException("Invalid user ID format: " + subject);
      }
    }
    throw new IllegalStateException("No authenticated user found");
  }

  public List<Wallet> list() {
    UUID userId = currentUserId();
    log.debug("Listing wallets for user: {}", userId);
    return walletRepo.findByUserIdOrderByCreatedAtDesc(userId);
  }

  public Wallet get(UUID id) {
    UUID userId = currentUserId();
    log.debug("Getting wallet {} for user: {}", id, userId);
    return walletRepo.findByIdAndUserId(id, userId)
      .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
  }

  @Transactional
  public Wallet create(WalletDtos.CreateReq req) {
    UUID userId = currentUserId();
    log.debug("Creating wallet '{}' for user: {}", req.name(), userId);
    
    // Check for unique name
    if (walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(userId, req.name())) {
      throw new IllegalArgumentException("Wallet name already exists");
    }
    
    // Validate goal amount for savings
    if (req.type() == Wallet.WalletType.SAVINGS && req.goalAmount() != null && req.goalAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Savings goal amount must be non-negative");
    }
    
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
      .isDefault(req.setDefault() || walletRepo.findByUserIdAndIsDefaultTrue(userId).isEmpty())
      .build();
    
    wallet = walletRepo.save(wallet);
    
    log.info("Created wallet '{}' with ID {} for user: {}", req.name(), wallet.getId(), userId);
    return wallet;
  }

  @Transactional
  public Wallet update(UUID id, WalletDtos.UpdateReq req) {
    UUID userId = currentUserId();
    log.debug("Updating wallet {} for user: {}", id, userId);
    
    Wallet wallet = get(id);
    
    // Check for unique name if changed
    if (!wallet.getName().equalsIgnoreCase(req.name()) && 
        walletRepo.existsByUserIdAndNameIgnoreCaseAndArchivedAtIsNull(userId, req.name())) {
      throw new IllegalArgumentException("Wallet name already exists");
    }
    
    // Validate goal amount for savings
    if (req.type() == Wallet.WalletType.SAVINGS && req.goalAmount() != null && req.goalAmount().compareTo(BigDecimal.ZERO) < 0) {
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
    UUID userId = currentUserId();
    log.debug("Archiving wallet {} for user: {}", id, userId);
    
    Wallet wallet = get(id);
    
    // If archiving the default wallet, find another wallet to reassign default to
    Wallet nextDefault = null;
    if (wallet.isDefault()) {
      List<Wallet> otherWallets = walletRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .filter(other -> other.getArchivedAt() == null && !other.getId().equals(wallet.getId()))
        .toList();
      
      if (!otherWallets.isEmpty()) {
        nextDefault = otherWallets.get(0);
        log.info("Will reassign default from wallet '{}' to '{}' for user: {}", 
                 wallet.getName(), nextDefault.getName(), userId);
      }
    }
    
    // Archive the wallet
    wallet.setArchivedAt(Instant.now());
    wallet.setArchivedBy(userId.toString());
    walletRepo.save(wallet);
    
    // Reassign default if needed
    if (nextDefault != null) {
      nextDefault.setDefault(true);
      walletRepo.save(nextDefault);
      log.info("Reassigned default to wallet '{}' for user: {}", nextDefault.getName(), userId);
    }
    
    log.info("Archived wallet '{}' with ID {} for user: {}", wallet.getName(), id, userId);
  }

  @Transactional
  public void activate(UUID id) {
    UUID userId = currentUserId();
    log.debug("Activating wallet {} for user: {}", id, userId);
    
    Wallet wallet = get(id);
    wallet.setArchivedAt(null);
    wallet.setArchivedBy(null);
    walletRepo.save(wallet);
    
    log.info("Activated wallet '{}' with ID {} for user: {}", wallet.getName(), id, userId);
  }

  @Transactional
  public void setDefault(UUID id) {
    UUID userId = currentUserId();
    log.debug("Setting wallet {} as default for user: {}", id, userId);
    
    Wallet wallet = get(id);
    
    // Unset current default if different
    walletRepo.findByUserIdAndIsDefaultTrue(userId).ifPresent(currentDefault -> {
      if (!currentDefault.getId().equals(wallet.getId())) {
        currentDefault.setDefault(false);
        walletRepo.save(currentDefault);
        log.debug("Unset previous default wallet '{}' for user: {}", currentDefault.getName(), userId);
      }
    });
    
    // Set new default
    wallet.setDefault(true);
    walletRepo.save(wallet);
    
    log.info("Set wallet '{}' as default for user: {}", wallet.getName(), userId);
  }
}
