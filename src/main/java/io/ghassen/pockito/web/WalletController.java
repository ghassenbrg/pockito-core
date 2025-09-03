package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.WalletType;
import io.ghassen.pockito.service.WalletService;
import io.ghassen.pockito.web.dto.WalletDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for wallet operations.
 * 
 * Provides HTTP endpoints for wallet CRUD operations and management.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    /**
     * Create a new wallet for the authenticated user.
     * 
     * @param walletDto the wallet data to create
     * @param authentication the authentication context
     * @return the created wallet
     */
    @PostMapping
    public ResponseEntity<WalletDto> createWallet(
            @Valid @RequestBody WalletDto walletDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating wallet for user: {}", username);
        
        // Username is automatically set by the service from SecurityUtils
        WalletDto createdWallet = walletService.createWallet(walletDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWallet);
    }

    /**
     * Get all wallets for the authenticated user.
     * 
     * @param authentication the authentication context
     * @return list of user's wallets
     */
    @GetMapping
    public ResponseEntity<List<WalletDto>> getUserWallets(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting wallets for user: {}", username);
        
        List<WalletDto> wallets = walletService.getUserWallets();
        return ResponseEntity.ok(wallets);
    }

    /**
     * Get a specific wallet by ID for the authenticated user.
     * 
     * @param walletId the wallet ID
     * @param authentication the authentication context
     * @return the wallet if found and owned by user
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDto> getWallet(
            @PathVariable UUID walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallet with ID: {} for user: {}", walletId, username);
        
        return walletService.getWalletById(walletId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing wallet for the authenticated user.
     * 
     * @param walletId the wallet ID to update
     * @param walletDto the updated wallet data
     * @param authentication the authentication context
     * @return the updated wallet
     */
    @PutMapping("/{walletId}")
    public ResponseEntity<WalletDto> updateWallet(
            @PathVariable UUID walletId,
            @Valid @RequestBody WalletDto walletDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating wallet with ID: {} for user: {}", walletId, username);
        
        // Username is automatically set by the service from SecurityUtils and cannot be updated
        try {
            WalletDto updatedWallet = walletService.updateWallet(walletId, walletDto);
            return ResponseEntity.ok(updatedWallet);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a wallet for the authenticated user.
     * 
     * @param walletId the wallet ID to delete
     * @param authentication the authentication context
     * @return no content on success
     */
    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable UUID walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Deleting wallet with ID: {} for user: {}", walletId, username);
        
        try {
            walletService.deleteWallet(walletId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get wallets by type for the authenticated user.
     * 
     * @param type the wallet type to filter by
     * @param authentication the authentication context
     * @return list of wallets of the specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<WalletDto>> getWalletsByType(
            @PathVariable WalletType type,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallets of type {} for user: {}", type, username);
        
        List<WalletDto> wallets = walletService.getUserWalletsByType(type);
        return ResponseEntity.ok(wallets);
    }

    /**
     * Get the default wallet for the authenticated user.
     * 
     * @param authentication the authentication context
     * @return the default wallet if exists
     */
    @GetMapping("/default")
    public ResponseEntity<WalletDto> getDefaultWallet(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting default wallet for user: {}", username);
        
        return walletService.getDefaultWallet()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Set a wallet as default for the authenticated user.
     * 
     * @param walletId the wallet ID to set as default
     * @param authentication the authentication context
     * @return the updated wallet
     */
    @PostMapping("/{walletId}/set-default")
    public ResponseEntity<WalletDto> setDefaultWallet(
            @PathVariable UUID walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Setting wallet with ID: {} as default for user: {}", walletId, username);
        
        try {
            WalletDto updatedWallet = walletService.setDefaultWallet(walletId);
            return ResponseEntity.ok(updatedWallet);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to set default wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reorder wallets for the authenticated user.
     * 
     * @param walletIds the list of wallet IDs in the new order
     * @param authentication the authentication context
     * @return no content on success
     */
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderWallets(
            @RequestBody List<UUID> walletIds,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Reordering {} wallets for user: {}", walletIds.size(), username);
        
        try {
            walletService.reorderWallets(walletIds);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to reorder wallets: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
