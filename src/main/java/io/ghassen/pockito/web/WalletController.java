package io.ghassen.pockito.web;

import io.ghassen.pockito.service.WalletService;
import io.ghassen.pockito.domain.enums.WalletType;
import io.ghassen.pockito.web.types.dto.WalletDto;
import io.ghassen.pockito.web.types.request.WalletRequest;
import io.ghassen.pockito.web.types.request.ReorderWalletsRequest;
import io.ghassen.pockito.web.types.response.WalletResponse;
import io.ghassen.pockito.web.types.response.WalletListResponse;
import io.ghassen.pockito.web.mapper.WalletMapper;
import io.ghassen.pockito.web.validation.ValidationGroups;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
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
    private final WalletMapper walletMapper;

    /**
     * Create a new wallet for the authenticated user.
     * 
     * @param walletRequest the wallet data to create
     * @param authentication the authentication context
     * @return the created wallet
     */
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Validated(ValidationGroups.Create.class) @RequestBody WalletRequest walletRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating wallet for user: {}", username);
        
        // Convert request to DTO for service layer
        WalletDto walletDto = walletMapper.requestToDto(walletRequest);
        
        WalletDto createdWalletDto = walletService.createWallet(walletDto);
        
        // Convert DTO to response
        WalletResponse walletResponse = walletMapper.dtoToResponse(createdWalletDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }

    /**
     * Get all wallets for the authenticated user.
     * 
     * @param authentication the authentication context
     * @return list of user's wallets
     */
    @GetMapping
    public ResponseEntity<WalletListResponse> getUserWallets(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting wallets for user: {}", username);
        
        List<WalletDto> walletDtos = walletService.getUserWallets();
        
        // Convert DTOs to responses
        List<WalletResponse> walletResponses = walletMapper.dtoListToResponseList(walletDtos);
        
        WalletListResponse response = WalletListResponse.builder()
            .wallets(walletResponses)
            .totalCount((long) walletResponses.size())
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific wallet by ID for the authenticated user.
     * 
     * @param walletId the wallet ID
     * @param authentication the authentication context
     * @return the wallet if found and owned by user
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable UUID walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallet with ID: {} for user: {}", walletId, username);
        
        return walletService.getWalletById(walletId)
            .map(walletMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing wallet for the authenticated user.
     * 
     * @param walletId the wallet ID to update
     * @param walletRequest the updated wallet data
     * @param authentication the authentication context
     * @return the updated wallet
     */
    @PutMapping("/{walletId}")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable UUID walletId,
            @Validated(ValidationGroups.Update.class) @RequestBody WalletRequest walletRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating wallet with ID: {} for user: {}", walletId, username);
        
        // Convert request to DTO for service layer
        WalletDto walletDto = walletMapper.requestToDto(walletRequest);
        
        try {
            WalletDto updatedWalletDto = walletService.updateWallet(walletId, walletDto);
            
            // Convert DTO to response
            WalletResponse walletResponse = walletMapper.dtoToResponse(updatedWalletDto);
            
            return ResponseEntity.ok(walletResponse);
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
    public ResponseEntity<WalletListResponse> getWalletsByType(
            @PathVariable WalletType type,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallets of type {} for user: {}", type, username);
        
        List<WalletDto> walletDtos = walletService.getUserWalletsByType(type);
        
        // Convert DTOs to responses
        List<WalletResponse> walletResponses = walletMapper.dtoListToResponseList(walletDtos);
        
        WalletListResponse response = WalletListResponse.builder()
            .wallets(walletResponses)
            .totalCount((long) walletResponses.size())
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get the default wallet for the authenticated user.
     * 
     * @param authentication the authentication context
     * @return the default wallet if exists
     */
    @GetMapping("/default")
    public ResponseEntity<WalletResponse> getDefaultWallet(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting default wallet for user: {}", username);
        
        return walletService.getDefaultWallet()
            .map(walletMapper::dtoToResponse)
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
    public ResponseEntity<WalletResponse> setDefaultWallet(
            @PathVariable UUID walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Setting wallet with ID: {} as default for user: {}", walletId, username);
        
        try {
            WalletDto updatedWalletDto = walletService.setDefaultWallet(walletId);
            
            WalletResponse walletResponse = walletMapper.dtoToResponse(updatedWalletDto);
            
            return ResponseEntity.ok(walletResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to set default wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reorder wallets for the authenticated user.
     * 
     * @param request the reorder request containing the list of wallet IDs in the new order
     * @param authentication the authentication context
     * @return no content on success
     */
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderWallets(
            @RequestBody @Valid ReorderWalletsRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Reordering {} wallets for user: {}", request.getWalletIds().size(), username);
        
        try {
            walletService.reorderWallets(request.getWalletIds());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to reorder wallets: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
