package io.ghassen.pockito.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.WalletType;
import io.ghassen.pockito.repo.TransactionRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.dto.WalletDto;
import io.ghassen.pockito.web.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for wallet business operations.
 * 
 * Provides business logic for wallet management including CRUD operations,
 * validation rules, and business constraints enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;

    /**
     * Create a new wallet for the authenticated user.
     * 
     * @param walletDto the wallet data to create
     * @return the created wallet DTO
     * @throws IllegalArgumentException if user not found or wallet name already
     *                                  exists
     */
    public WalletDto createWallet(WalletDto walletDto) {
        // Automatically set username from authenticated user
        String username = SecurityUtils.getCurrentUserId();
        walletDto.setUsername(username);
        
        log.debug("Creating wallet for user: {}", username);

        // Validate user exists
        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Check if wallet name already exists for this user
        if (walletRepository.existsByUserUsernameAndName(username, walletDto.getName())) {
            throw new IllegalArgumentException("Wallet with name '" + walletDto.getName()
                    + "' already exists for user: " + username);
        }

        // Set order position
        int maxOrder = walletRepository.findMaxOrderPositionByUserUsername(username);
            walletDto.setOrderPosition(maxOrder + 1);

        // Set default flag if not provided
        if (walletDto.getIsDefault() == null) {
            walletDto.setIsDefault(false);
        }

        // Convert to entity and save
        Wallet wallet = walletMapper.toEntity(walletDto);
        wallet.setUser(user);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created wallet with ID: {} for user: {}", savedWallet.getId(), username);

        // If this wallet is marked as default, update all wallets atomically
        if (walletDto.getIsDefault()) {
            walletRepository.setDefaultWalletForUser(username, savedWallet.getId());
            // Refresh the saved wallet to get updated state
            savedWallet = walletRepository.findById(savedWallet.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found after setting as default"));
        }

        WalletDto createdWalletDto = walletMapper.toDto(savedWallet);
        setDerivedFields(createdWalletDto, savedWallet);
        return createdWalletDto;
    }

    /**
     * Get all wallets for the authenticated user.
     * 
     * @return list of wallet DTOs ordered by position
     */
    @Transactional(readOnly = true)
    public List<WalletDto> getUserWallets() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallets for user: {}", username);
        List<Wallet> wallets = walletRepository.findByUserUsernameOrderByOrderPositionAsc(username);
        List<WalletDto> walletDtos = walletMapper.toDtoList(wallets);
        
        // Set derived fields for each wallet
        for (int i = 0; i < wallets.size(); i++) {
            setDerivedFields(walletDtos.get(i), wallets.get(i));
        }
        
        log.info("Retrieved {} wallets for user: {}", walletDtos.size(), username);
        return walletDtos;
    }

    /**
     * Get wallet by ID for the authenticated user.
     * 
     * @param walletId the wallet ID
     * @return the wallet DTO if found and owned by user
     */
    @Transactional(readOnly = true)
    public Optional<WalletDto> getWalletById(UUID walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallet with ID: {} for user: {}", walletId, username);
        Optional<WalletDto> walletDto = walletRepository.findById(walletId)
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .map(walletMapper::toDto);
        
        if (walletDto.isPresent()) {
            // Set derived fields for the wallet
            Wallet wallet = walletRepository.findById(walletId)
                    .filter(w -> w.getUser().getUsername().equals(username))
                    .orElse(null);
            if (wallet != null) {
                setDerivedFields(walletDto.get(), wallet);
            }
            log.info("Retrieved wallet with ID: {} for user: {}", walletId, username);
        } else {
            log.info("Wallet with ID: {} not found or access denied for user: {}", walletId, username);
        }
        
        return walletDto;
    }

    /**
     * Update an existing wallet for the authenticated user.
     * 
     * @param walletId  the wallet ID to update
     * @param walletDto the updated wallet data
     * @return the updated wallet DTO
     * @throws IllegalArgumentException if wallet not found, not owned by user, or
     *                                  validation fails
     */
    public WalletDto updateWallet(UUID walletId, WalletDto walletDto) {
        // Automatically set username from authenticated user and prevent username updates
        String username = SecurityUtils.getCurrentUserId();
        walletDto.setUsername(username);
        
        log.debug("Updating wallet with ID: {} for user: {}", walletId, username);

        Wallet existingWallet = walletRepository.findById(walletId)
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        // Check if name change would conflict with existing wallet
        if (!existingWallet.getName().equals(walletDto.getName()) &&
                walletRepository.existsByUserUsernameAndName(username, walletDto.getName())) {
            throw new IllegalArgumentException(
                    "Wallet with name '" + walletDto.getName() + "' already exists for user: " + username);
        }

        walletDto.setOrderPosition(existingWallet.getOrderPosition());
        
        // Currency should not be updated
        existingWallet.setCurrency(walletDto.getCurrency());

        // Update entity with new data
        walletMapper.updateEntityFromDto(walletDto, existingWallet);

        Wallet updatedWallet = walletRepository.save(existingWallet);

        // Handle default wallet change if needed
        if (walletDto.getIsDefault() && !existingWallet.getIsDefault()) {
            walletRepository.setDefaultWalletForUser(username, walletId);
            // Refresh the updated wallet to get current state
            updatedWallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found after setting as default"));
        }

        log.info("Updated wallet with ID: {} for user: {}", walletId, username);
        WalletDto updatedWalletDto = walletMapper.toDto(updatedWallet);
        setDerivedFields(updatedWalletDto, updatedWallet);
        return updatedWalletDto;
    }

    /**
     * Delete a wallet for the authenticated user.
     * 
     * @param walletId the wallet ID to delete
     * @throws IllegalArgumentException if wallet not found or not owned by user
     */
    public void deleteWallet(UUID walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting wallet with ID: {} for user: {}", walletId, username);

        Wallet wallet = walletRepository.findById(walletId)
                .filter(w -> w.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        // If this was the default wallet, we might want to set another one as default
        if (wallet.getIsDefault()) {
            log.warn("Deleting default wallet for user: {}. Consider setting a new default wallet.", username);
        }

        walletRepository.delete(wallet);
        log.info("Deleted wallet with ID: {} for user: {}", walletId, username);
    }

    /**
     * Get wallets by type for the authenticated user.
     * 
     * @param type the wallet type to filter by
     * @return list of wallet DTOs of the specified type
     */
    @Transactional(readOnly = true)
    public List<WalletDto> getUserWalletsByType(WalletType type) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallets of type {} for user: {}", type, username);
        List<Wallet> wallets = walletRepository.findByUserUsernameAndTypeOrderByOrderPositionAsc(username, type);
        List<WalletDto> walletDtos = walletMapper.toDtoList(wallets);
        
        // Set derived fields for each wallet
        for (int i = 0; i < wallets.size(); i++) {
            setDerivedFields(walletDtos.get(i), wallets.get(i));
        }
        
        log.info("Retrieved {} wallets of type {} for user: {}", walletDtos.size(), type, username);
        return walletDtos;
    }

    /**
     * Get the default wallet for the authenticated user.
     * 
     * @return the default wallet DTO if exists
     */
    @Transactional(readOnly = true)
    public Optional<WalletDto> getDefaultWallet() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting default wallet for user: {}", username);
        Optional<WalletDto> defaultWallet = walletRepository.findByUserUsernameAndIsDefaultTrue(username)
                .map(walletMapper::toDto);
        
        if (defaultWallet.isPresent()) {
            // Set derived fields for the wallet
            Wallet wallet = walletRepository.findByUserUsernameAndIsDefaultTrue(username).orElse(null);
            if (wallet != null) {
                setDerivedFields(defaultWallet.get(), wallet);
            }
            log.info("Retrieved default wallet with ID: {} for user: {}", defaultWallet.get().getId(), username);
        } else {
            log.info("No default wallet found for user: {}", username);
        }
        
        return defaultWallet;
    }

    /**
     * Set a wallet as default for the authenticated user.
     * 
     * @param walletId the wallet ID to set as default
     * @return the updated wallet DTO
     * @throws IllegalArgumentException if wallet not found or not owned by user
     */
    public WalletDto setDefaultWallet(UUID walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Setting wallet with ID: {} as default for user: {}", walletId, username);

        Wallet wallet = walletRepository.findById(walletId)
                .filter(w -> w.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        // Use repository method to set default wallet and update all others atomically
        walletRepository.setDefaultWalletForUser(username, walletId);

        // Refresh the wallet entity to get updated state
        wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found after update"));

        log.info("Set wallet with ID: {} as default for user: {}", walletId, username);
        WalletDto defaultWalletDto = walletMapper.toDto(wallet);
        setDerivedFields(defaultWalletDto, wallet);
        return defaultWalletDto;
    }

    /**
     * Reorder wallets for the authenticated user.
     * 
     * @param walletIds the list of wallet IDs in the new order
     * @throws IllegalArgumentException if any wallet not found or not owned by user
     */
    public void reorderWallets(List<UUID> walletIds) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Reordering wallets for user: {}", username);

        if (walletIds == null || walletIds.isEmpty()) {
            throw new IllegalArgumentException("Wallet IDs list cannot be null or empty");
        }

        // Validate all wallets belong to the user
        List<Wallet> wallets = walletRepository.findAllById(walletIds);
        if (wallets.size() != walletIds.size()) {
            throw new IllegalArgumentException("Some wallets not found");
        }

        for (Wallet wallet : wallets) {
            if (!wallet.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException("Access denied to wallet: " + wallet.getId());
            }
        }

        // Update order positions
        for (int i = 0; i < walletIds.size(); i++) {
            UUID walletId = walletIds.get(i);
            Wallet wallet = wallets.stream()
                    .filter(w -> w.getId().equals(walletId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + walletId));

            wallet.setOrderPosition(i + 1);
        }

        walletRepository.saveAll(wallets);
        log.info("Reordered {} wallets for user: {}", walletIds.size(), username);
    }

    /**
     * Set derived fields for a wallet DTO.
     * 
     * @param walletDto the wallet DTO to set derived fields for
     * @param wallet the wallet entity to get archivedAt information from
     */
    private void setDerivedFields(WalletDto walletDto, Wallet wallet) {
        // Calculate current balance based on transactions
        BigDecimal currentBalance = calculateCurrentBalance(wallet);
        walletDto.setBalance(currentBalance);

        // Set active based on archivedAt (true if not archived, false if archived)
        walletDto.setActive(wallet.getArchivedAt() == null);
    }

    /**
     * Calculate the current balance of a wallet based on transactions.
     * Uses the repository method to calculate balance in a single database query.
     * 
     * @param wallet the wallet to calculate balance for
     * @return the current balance of the wallet
     */
    private BigDecimal calculateCurrentBalance(Wallet wallet) {
        try {
            BigDecimal currentBalance = transactionRepository.calculateCurrentBalance(wallet.getId());
            
            log.debug("Calculated balance for wallet {}: initial={}, current={}", 
                    wallet.getId(), wallet.getInitialBalance(), currentBalance);
            
            return currentBalance;
        } catch (Exception e) {
            log.warn("Error calculating balance for wallet {}, falling back to initial balance: {}", 
                    wallet.getId(), e.getMessage());
            return wallet.getInitialBalance();
        }
    }

}
