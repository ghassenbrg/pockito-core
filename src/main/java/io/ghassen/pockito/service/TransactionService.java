package io.ghassen.pockito.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.repo.TransactionRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.mapper.TransactionMapper;
import io.ghassen.pockito.web.types.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for transaction business operations.
 * 
 * Provides business logic for transaction management including CRUD operations,
 * validation rules, and business constraints enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Create a new transaction for the authenticated user.
     * 
     * @param transactionDto the transaction data to create
     * @return the created transaction DTO
     * @throws IllegalArgumentException if validation fails or related entities not
     *                                  found
     */
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        // Automatically set username from authenticated user
        String username = SecurityUtils.getCurrentUserId();
        transactionDto.setUsername(username);

        log.info("Creating transaction for user: {}", username);

        // Validate and set related entities
        Transaction transaction = validateAndBuildTransaction(transactionDto);

        // Save the transaction
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction with ID: {} for user: {}", savedTransaction.getId(), username);

        return transactionMapper.toDto(savedTransaction);
    }

    /**
     * Update an existing transaction for the authenticated user.
     * 
     * @param transactionId  the transaction ID to update
     * @param transactionDto the updated transaction data
     * @return the updated transaction DTO
     * @throws IllegalArgumentException if transaction not found, not owned by user,
     *                                  or validation fails
     */
    public TransactionDto updateTransaction(String transactionId, TransactionDto transactionDto) {
        // Automatically set username from authenticated user and prevent username
        // updates
        String username = SecurityUtils.getCurrentUserId();
        transactionDto.setUsername(username);

        log.info("Updating transaction with ID: {} for user: {}", transactionId, username);

        // Find existing transaction and verify ownership
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));

        // Validate and update the transaction
        validateAndUpdateTransaction(transactionDto, existingTransaction);

        // Save the updated transaction
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        log.info("Updated transaction with ID: {} for user: {}", transactionId, username);

        return transactionMapper.toDto(updatedTransaction);
    }

    /**
     * Get a transaction by ID for the authenticated user.
     * 
     * @param transactionId the transaction ID
     * @return the transaction DTO if found and owned by user
     */
    @Transactional(readOnly = true)
    public Optional<TransactionDto> getTransactionById(String transactionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting transaction with ID: {} for user: {}", transactionId, username);

        Optional<TransactionDto> transactionDto = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUser().getUsername().equals(username))
                .map(transactionMapper::toDto);

        if (transactionDto.isPresent()) {
            log.info("Retrieved transaction with ID: {} for user: {}", transactionId, username);
        } else {
            log.info("Transaction with ID: {} not found or access denied for user: {}", transactionId, username);
        }

        return transactionDto;
    }

    /**
     * Delete a transaction for the authenticated user.
     * 
     * @param transactionId the transaction ID to delete
     * @throws IllegalArgumentException if transaction not found or not owned by
     *                                  user
     */
    public void deleteTransaction(String transactionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.info("Deleting transaction with ID: {} for user: {}", transactionId, username);

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));

        transactionRepository.delete(transaction);
        log.info("Deleted transaction with ID: {} for user: {}", transactionId, username);
    }

    /**
     * Get transactions for the authenticated user with filtering and pagination.
     * 
     * @param walletId        optional wallet ID to filter by
     * @param startDate       optional start date for date range filtering
     * @param endDate         optional end date for date range filtering
     * @param transactionType optional transaction type to filter by
     * @param pageable        pagination information
     * @return page of transaction DTOs matching the criteria
     */
    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByCriteria(
            String walletId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType transactionType,
            Pageable pageable) {

        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting transactions for user: {} with filters - walletId: {}, startDate: {}, endDate: {}, type: {}",
                username, walletId, startDate, endDate, transactionType);

        // If walletId is provided, verify the wallet belongs to the user
        if (walletId != null) {
            walletRepository.findById(walletId)
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));
        }

        Page<Transaction> transactions = transactionRepository.findByUserWithFilters(
                username, walletId, startDate, endDate, transactionType, pageable);

        Page<TransactionDto> transactionDtos = transactions.map(transactionMapper::toDto);

        log.info("Retrieved {} transactions for user: {} (page {}/{})",
                transactionDtos.getNumberOfElements(), username,
                pageable.getPageNumber() + 1, transactionDtos.getTotalPages());

        return transactionDtos;
    }

    /**
     * Get all transactions for the authenticated user (without pagination).
     * 
     * @return list of all transaction DTOs for the user
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> getAllUserTransactions() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting all transactions for user: {}", username);

        List<Transaction> transactions = transactionRepository.findByUserUsernameOrderByEffectiveDateDesc(username);
        List<TransactionDto> transactionDtos = transactionMapper.toDtoList(transactions);

        log.info("Retrieved {} transactions for user: {}", transactionDtos.size(), username);
        return transactionDtos;
    }

    /**
     * Validate transaction data and build Transaction entity.
     * 
     * @param transactionDto the transaction DTO to validate
     * @return the validated Transaction entity
     * @throws IllegalArgumentException if validation fails
     */
    private Transaction validateAndBuildTransaction(TransactionDto transactionDto) {

        // Validate that the username in the DTO matches the authenticated user
        String currentUser = SecurityUtils.getCurrentUserId();
        if (transactionDto.getUsername() != null && !transactionDto.getUsername().equals(currentUser)) {
            throw new IllegalArgumentException("Username in transaction does not match the authenticated user");
        }
        // Validate user exists
        User user = userRepository.findById(transactionDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + transactionDto.getUsername()));

        // Validate transaction type specific rules
        validateTransactionTypeRules(transactionDto);

        // Build the transaction entity
        Transaction transaction = transactionMapper.toEntity(transactionDto);
        transaction.setUser(user);

        // Set related entities
        setRelatedEntities(transaction, transactionDto);

        // Set exchange rate based on wallet currencies
        setExchangeRate(transaction);

        return transaction;
    }

    /**
     * Validate and update existing transaction entity.
     * 
     * @param transactionDto      the transaction DTO with update data
     * @param existingTransaction the existing transaction entity to update
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAndUpdateTransaction(TransactionDto transactionDto, Transaction existingTransaction) {
        // Validate transaction type specific rules
        validateTransactionTypeRules(transactionDto);

        // Update the entity with new data
        transactionMapper.updateEntityFromDto(transactionDto, existingTransaction);

        // Set related entities
        setRelatedEntities(existingTransaction, transactionDto);

        // Set exchange rate based on wallet currencies
        setExchangeRate(existingTransaction);
    }

    /**
     * Validate transaction type specific business rules.
     * 
     * @param transactionDto the transaction DTO to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTransactionTypeRules(TransactionDto transactionDto) {
        TransactionType type = transactionDto.getTransactionType();

        switch (type) {
            case EXPENSE:
                // Force walletTo to be null for expense transactions
                if (transactionDto.getWalletToId() != null) {
                    log.debug("Forcing walletTo to null for EXPENSE transaction");
                    transactionDto.setWalletToId(null);
                }
                if (transactionDto.getWalletFromId() == null) {
                    throw new IllegalArgumentException("EXPENSE transactions require walletFrom to be set");
                }
                break;

            case INCOME:
                // Force walletFrom to be null for income transactions
                if (transactionDto.getWalletFromId() != null) {
                    log.debug("Forcing walletFrom to null for INCOME transaction");
                    transactionDto.setWalletFromId(null);
                }
                if (transactionDto.getWalletToId() == null) {
                    throw new IllegalArgumentException("INCOME transactions require walletTo to be set");
                }
                break;

            case TRANSFER:
                if (transactionDto.getWalletFromId() == null && transactionDto.getWalletToId() == null) {
                    throw new IllegalArgumentException(
                            "TRANSFER transactions require at least one of walletFrom or walletTo to be set");
                } else if (transactionDto.getWalletFromId() != null && transactionDto.getWalletToId() != null
                        && transactionDto.getWalletFromId().equals(transactionDto.getWalletToId())) {
                    throw new IllegalArgumentException("Source wallet and destination wallet cannot be the same");
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
    }

    /**
     * Set related entities (wallets and category) for the transaction.
     * 
     * @param transaction    the transaction entity
     * @param transactionDto the transaction DTO
     * @throws IllegalArgumentException if related entities not found or not owned
     *                                  by user
     */
    private void setRelatedEntities(Transaction transaction, TransactionDto transactionDto) {
        String username = transactionDto.getUsername();

        // Set walletFrom if provided, otherwise clear it
        if (transactionDto.getWalletFromId() != null) {
            Wallet walletFrom = walletRepository.findById(transactionDto.getWalletFromId())
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Source wallet not found or access denied"));
            transaction.setWalletFrom(walletFrom);
        } else {
            transaction.setWalletFrom(null);
        }

        // Set walletTo if provided, otherwise clear it
        if (transactionDto.getWalletToId() != null) {
            Wallet walletTo = walletRepository.findById(transactionDto.getWalletToId())
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found or access denied"));
            transaction.setWalletTo(walletTo);
        } else {
            transaction.setWalletTo(null);
        }

        // Set category if provided
        if (transactionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(transactionDto.getCategoryId())
                    .filter(cat -> cat.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
            transaction.setCategory(category);
        }
    }

    /**
     * Set exchange rate based on wallet currencies.
     * If both wallets have the same currency, or if one wallet is NULL, then
     * exchangeRate = 1.
     * Otherwise, use the provided exchange rate.
     * 
     * @param transaction the transaction entity
     */
    private void setExchangeRate(Transaction transaction) {
        Wallet walletFrom = transaction.getWalletFrom();
        Wallet walletTo = transaction.getWalletTo();

        // If both wallets have the same currency, or if one wallet is NULL, then
        // exchangeRate = 1
        if (walletFrom == null || walletTo == null ||
                walletFrom.getCurrency().equals(walletTo.getCurrency())) {
            transaction.setExchangeRate(BigDecimal.ONE);
        }
        // Otherwise, keep the provided exchange rate (already set in DTO)
    }
}
