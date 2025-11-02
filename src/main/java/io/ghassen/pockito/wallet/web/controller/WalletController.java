package io.ghassen.pockito.wallet.web.controller;

import io.ghassen.pockito.wallet.application.service.WalletService;
import io.ghassen.pockito.domain.enums.WalletType;
import io.ghassen.pockito.domain.validation.ValidationGroups;
import io.ghassen.pockito.wallet.application.dto.WalletDto;
import io.ghassen.pockito.wallet.web.api.request.WalletRequest;
import io.ghassen.pockito.wallet.web.api.request.ReorderWalletsRequest;
import io.ghassen.pockito.wallet.web.api.response.WalletResponse;
import io.ghassen.pockito.wallet.web.api.response.WalletListResponse;
import io.ghassen.pockito.wallet.web.mapper.WalletApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final WalletApiMapper walletMapper;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Validated(ValidationGroups.Create.class) @RequestBody WalletRequest walletRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Creating wallet for user: {}", username);
        
        WalletDto walletDto = walletMapper.requestToDto(walletRequest);
        
        WalletDto createdWalletDto = walletService.createWallet(walletDto);
        
        WalletResponse walletResponse = walletMapper.dtoToResponse(createdWalletDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }

    @GetMapping
    public ResponseEntity<WalletListResponse> getUserWallets(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting wallets for user: {}", username);
        
        List<WalletDto> walletDtos = walletService.getUserWallets();
        
        List<WalletResponse> walletResponses = walletMapper.dtoListToResponseList(walletDtos);
        
        WalletListResponse response = WalletListResponse.builder()
            .wallets(walletResponses)
            .totalCount((long) walletResponses.size())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable String walletId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallet with ID: {} for user: {}", walletId, username);
        
        return walletService.getWalletById(walletId)
            .map(walletMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<WalletResponse> updateWallet(
            @PathVariable String walletId,
            @Validated(ValidationGroups.Update.class) @RequestBody WalletRequest walletRequest,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Updating wallet with ID: {} for user: {}", walletId, username);
        
        WalletDto walletDto = walletMapper.requestToDto(walletRequest);
        
        try {
            WalletDto updatedWalletDto = walletService.updateWallet(walletId, walletDto);
            
            WalletResponse walletResponse = walletMapper.dtoToResponse(updatedWalletDto);
            
            return ResponseEntity.ok(walletResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<Void> deleteWallet(
            @PathVariable String walletId,
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

    @GetMapping("/type/{type}")
    public ResponseEntity<WalletListResponse> getWalletsByType(
            @PathVariable WalletType type,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.debug("Getting wallets of type {} for user: {}", type, username);
        
        List<WalletDto> walletDtos = walletService.getUserWalletsByType(type);
        
        List<WalletResponse> walletResponses = walletMapper.dtoListToResponseList(walletDtos);
        
        WalletListResponse response = WalletListResponse.builder()
            .wallets(walletResponses)
            .totalCount((long) walletResponses.size())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    public ResponseEntity<WalletResponse> getDefaultWallet(Authentication authentication) {
        String username = authentication.getName();
        log.debug("Getting default wallet for user: {}", username);
        
        return walletService.getDefaultWallet()
            .map(walletMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{walletId}/set-default")
    public ResponseEntity<WalletResponse> setDefaultWallet(
            @PathVariable String walletId,
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


