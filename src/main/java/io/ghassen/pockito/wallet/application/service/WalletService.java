package io.ghassen.pockito.wallet.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.domain.enums.WalletType;
import io.ghassen.pockito.transaction.infrastructure.persistence.repository.TransactionRepository;
import io.ghassen.pockito.user.infrastructure.persistence.repository.UserRepository;
import io.ghassen.pockito.wallet.infrastructure.persistence.repository.WalletRepository;
import io.ghassen.pockito.shared.security.SecurityUtils;
import io.ghassen.pockito.wallet.application.dto.WalletDto;
import io.ghassen.pockito.wallet.application.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;

    public WalletDto createWallet(WalletDto walletDto) {
        String username = SecurityUtils.getCurrentUserId();
        walletDto.setUsername(username);

        log.debug("Creating wallet for user: {}", username);

        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (walletRepository.existsByUserUsernameAndName(username, walletDto.getName())) {
            throw new IllegalArgumentException("Wallet with name '" + walletDto.getName()
                    + "' already exists for user: " + username);
        }

        int maxOrder = walletRepository.findMaxOrderPositionByUserUsername(username);
        walletDto.setOrderPosition(maxOrder + 1);

        long walletCount = walletRepository.countByUserUsername(username);
        boolean isFirstWallet = walletCount == 0;

        if (isFirstWallet) {
            walletDto.setIsDefault(true);
            log.debug("This is the user's first wallet - automatically setting as default");
        }

        Wallet wallet = walletMapper.toEntity(walletDto);
        wallet.setUser(user);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created wallet with ID: {} for user: {}", savedWallet.getId(), username);

        if (walletDto.getIsDefault()) {
            walletRepository.setDefaultWalletForUser(username, savedWallet.getId());
            savedWallet = walletRepository.findById(savedWallet.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found after setting as default"));
        }

        WalletDto createdWalletDto = walletMapper.toDto(savedWallet);
        setDerivedFields(createdWalletDto, savedWallet);
        return createdWalletDto;
    }

    @Transactional(readOnly = true)
    public List<WalletDto> getUserWallets() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallets for user: {}", username);
        List<Wallet> wallets = walletRepository.findByUserUsernameOrderByOrderPositionAsc(username);
        List<WalletDto> walletDtos = walletMapper.toDtoList(wallets);

        for (int i = 0; i < wallets.size(); i++) {
            setDerivedFields(walletDtos.get(i), wallets.get(i));
        }

        log.info("Retrieved {} wallets for user: {}", walletDtos.size(), username);
        return walletDtos;
    }

    @Transactional(readOnly = true)
    public Optional<WalletDto> getWalletById(String walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallet with ID: {} for user: {}", walletId, username);
        Optional<WalletDto> walletDto = walletRepository.findById(walletId)
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .map(walletMapper::toDto);

        if (walletDto.isPresent()) {
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

    public WalletDto updateWallet(String walletId, WalletDto walletDto) {
        String username = SecurityUtils.getCurrentUserId();
        walletDto.setUsername(username);

        log.debug("Updating wallet with ID: {} for user: {}", walletId, username);

        Wallet existingWallet = walletRepository.findById(walletId)
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        if (!existingWallet.getName().equals(walletDto.getName()) &&
                walletRepository.existsByUserUsernameAndName(username, walletDto.getName())) {
            throw new IllegalArgumentException(
                    "Wallet with name '" + walletDto.getName() + "' already exists for user: " + username);
        }

        walletDto.setOrderPosition(existingWallet.getOrderPosition());

        walletMapper.updateEntityFromDto(walletDto, existingWallet);

        Wallet updatedWallet = walletRepository.save(existingWallet);

        if (walletDto.getIsDefault() != null && walletDto.getIsDefault()) {
            walletRepository.setDefaultWalletForUser(username, walletId);
            updatedWallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found after update"));
        }

        log.info("Updated wallet with ID: {} for user: {}", walletId, username);
        WalletDto updatedWalletDto = walletMapper.toDto(updatedWallet);
        setDerivedFields(updatedWalletDto, updatedWallet);
        return updatedWalletDto;
    }

    public void deleteWallet(String walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting wallet with ID: {} for user: {}", walletId, username);

        Wallet wallet = walletRepository.findById(walletId)
                .filter(w -> w.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        if (wallet.getIsDefault()) {
            log.warn("Deleting default wallet for user: {}. Setting first wallet as new default.", username);
            walletRepository.setFirstRemainingWalletAsDefault(username, walletId);
            log.info("Successfully set first wallet as new default for user: {}", username);
        }

        handleRelatedTransactions(walletId, username);

        walletRepository.delete(wallet);
        log.info("Deleted wallet with ID: {} for user: {}", walletId, username);
    }

    private void handleRelatedTransactions(String walletId, String username) {
        log.debug("Handling related transactions for wallet {} belonging to user {}", walletId, username);
        
        List<Transaction> relatedTransactions = transactionRepository.findAllByWalletId(walletId);
        
        if (relatedTransactions.isEmpty()) {
            log.debug("No related transactions found for wallet {}", walletId);
            return;
        }
        
        log.info("Found {} related transactions for wallet {}", relatedTransactions.size(), walletId);
        
        for (Transaction transaction : relatedTransactions) {
            if (!transaction.getUser().getUsername().equals(username)) {
                log.warn("Skipping transaction {} - user mismatch", transaction.getId());
                continue;
            }
            
            handleTransaction(transaction, walletId);
        }
        
        log.info("Completed handling transactions for wallet {}", walletId);
    }

    private void handleTransaction(Transaction transaction, String deletedWalletId) {
        io.ghassen.pockito.domain.enums.TransactionType type = transaction.getTransactionType();
        String walletFromId = transaction.getWalletFrom() != null ? transaction.getWalletFrom().getId() : null;
        String walletToId = transaction.getWalletTo() != null ? transaction.getWalletTo().getId() : null;
        
        boolean isFromWallet = deletedWalletId.equals(walletFromId);
        boolean isToWallet = deletedWalletId.equals(walletToId);
        
        if (type == TransactionType.EXPENSE || type == TransactionType.INCOME) {
            transactionRepository.delete(transaction);
            
        } else if (type == TransactionType.TRANSFER) {
            boolean fromIsNull = walletFromId == null;
            boolean toIsNull = walletToId == null;
            
            if (fromIsNull || toIsNull) {
                transactionRepository.delete(transaction);
                
            } else if (isFromWallet && !isToWallet) {
                transaction.setWalletFrom(null);
                transactionRepository.save(transaction);
                
            } else if (isToWallet && !isFromWallet) {
                transaction.setWalletTo(null);
                transactionRepository.save(transaction);
                
            } else {
                log.warn("Deleting TRANSFER transaction {} - both wallets reference deleted wallet", transaction.getId());
                transactionRepository.delete(transaction);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<WalletDto> getUserWalletsByType(WalletType type) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting wallets of type {} for user: {}", type, username);
        List<Wallet> wallets = walletRepository.findByUserUsernameAndTypeOrderByOrderPositionAsc(username, type);
        List<WalletDto> walletDtos = walletMapper.toDtoList(wallets);

        for (int i = 0; i < wallets.size(); i++) {
            setDerivedFields(walletDtos.get(i), wallets.get(i));
        }

        log.info("Retrieved {} wallets of type {} for user: {}", walletDtos.size(), type, username);
        return walletDtos;
    }

    @Transactional(readOnly = true)
    public Optional<WalletDto> getDefaultWallet() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting default wallet for user: {}", username);
        Optional<WalletDto> defaultWallet = walletRepository.findByUserUsernameAndIsDefaultTrue(username)
                .map(walletMapper::toDto);

        if (defaultWallet.isPresent()) {
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

    public WalletDto setDefaultWallet(String walletId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Setting wallet with ID: {} as default for user: {}", walletId, username);

        Wallet wallet = walletRepository.findById(walletId)
                .filter(w -> w.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));

        walletRepository.setDefaultWalletForUser(username, walletId);

        wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found after update"));

        log.info("Set wallet with ID: {} as default for user: {}", walletId, username);
        WalletDto defaultWalletDto = walletMapper.toDto(wallet);
        setDerivedFields(defaultWalletDto, wallet);
        return defaultWalletDto;
    }

    public void reorderWallets(List<String> walletIds) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Reordering wallets for user: {}", username);

        if (walletIds == null || walletIds.isEmpty()) {
            throw new IllegalArgumentException("Wallet IDs list cannot be null or empty");
        }

        List<Wallet> wallets = walletRepository.findAllById(walletIds);
        if (wallets.size() != walletIds.size()) {
            throw new IllegalArgumentException("Some wallets not found");
        }

        for (Wallet wallet : wallets) {
            if (!wallet.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException("Access denied to wallet: " + wallet.getId());
            }
        }

        for (int i = 0; i < walletIds.size(); i++) {
            String id = walletIds.get(i);
            Wallet wallet = wallets.stream()
                    .filter(w -> w.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + id));

            wallet.setOrderPosition(i + 1);
        }

        walletRepository.saveAll(wallets);
        log.info("Reordered {} wallets for user: {}", walletIds.size(), username);
    }

    private void setDerivedFields(WalletDto walletDto, Wallet wallet) {
        BigDecimal currentBalance = calculateCurrentBalance(wallet);
        walletDto.setBalance(currentBalance);

        walletDto.setActive(wallet.getArchivedAt() == null);
    }

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


