package io.ghassen.pockito.transaction.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.Subscription;
import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.category.infrastructure.persistence.repository.CategoryRepository;
import io.ghassen.pockito.subscription.infrastructure.persistence.repository.SubscriptionRepository;
import io.ghassen.pockito.transaction.infrastructure.persistence.repository.TransactionRepository;
import io.ghassen.pockito.user.infrastructure.persistence.repository.UserRepository;
import io.ghassen.pockito.wallet.infrastructure.persistence.repository.WalletRepository;
import io.ghassen.pockito.shared.security.SecurityUtils;
import io.ghassen.pockito.transaction.application.dto.TransactionDto;
import io.ghassen.pockito.transaction.application.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionDto createTransaction(TransactionDto transactionDto) {
        String username = SecurityUtils.getCurrentUserId();
        transactionDto.setUsername(username);

        log.info("Creating transaction for user: {}", username);

        Transaction transaction = validateAndBuildTransaction(transactionDto, null);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction with ID: {} for user: {}", savedTransaction.getId(), username);

        return transactionMapper.toDto(savedTransaction);
    }

    public TransactionDto updateTransaction(String transactionId, TransactionDto transactionDto) {
        String username = SecurityUtils.getCurrentUserId();
        transactionDto.setUsername(username);

        log.info("Updating transaction with ID: {} for user: {}", transactionId, username);

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));

        validateAndUpdateTransaction(transactionDto, existingTransaction);

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);
        log.info("Updated transaction with ID: {} for user: {}", transactionId, username);

        return transactionMapper.toDto(updatedTransaction);
    }

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

    public void deleteTransaction(String transactionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.info("Deleting transaction with ID: {} for user: {}", transactionId, username);

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));

        transactionRepository.delete(transaction);
        log.info("Deleted transaction with ID: {} for user: {}", transactionId, username);
    }

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

    @Transactional(readOnly = true)
    public List<TransactionDto> getAllUserTransactions() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting all transactions for user: {}", username);

        List<Transaction> transactions = transactionRepository.findByUserUsernameOrderByEffectiveDateDesc(username);
        List<TransactionDto> transactionDtos = transactionMapper.toDtoList(transactions);

        log.info("Retrieved {} transactions for user: {}", transactionDtos.size(), username);
        return transactionDtos;
    }

    private Transaction validateAndBuildTransaction(TransactionDto transactionDto, Transaction existingTransaction) {

        String currentUser = SecurityUtils.getCurrentUserId();
        if (transactionDto.getUsername() != null && !transactionDto.getUsername().equals(currentUser)) {
            throw new IllegalArgumentException("Username in transaction does not match the authenticated user");
        }
        User user = userRepository.findById(transactionDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + transactionDto.getUsername()));

        validateTransactionTypeRules(transactionDto, existingTransaction);

        Transaction transaction = transactionMapper.toEntity(transactionDto);
        transaction.setUser(user);

        setRelatedEntities(transaction, transactionDto);

        setExchangeRate(transaction);

        return transaction;
    }

    private void validateAndUpdateTransaction(TransactionDto transactionDto, Transaction existingTransaction) {
        validateTransactionTypeRules(transactionDto, existingTransaction);

        transactionMapper.updateEntityFromDto(transactionDto, existingTransaction);

        setRelatedEntities(existingTransaction, transactionDto);

        setExchangeRate(existingTransaction);
    }

    private void validateTransactionTypeRules(TransactionDto transactionDto, Transaction existingTransaction) {
        TransactionType type = transactionDto.getTransactionType();
        
        boolean hasSubscription = transactionDto.getSubscriptionId() != null || 
                                 (existingTransaction != null && existingTransaction.getSubscription() != null);
        
        if (hasSubscription && type != TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Transactions with a subscription must be of type EXPENSE");
        }
        
        if (hasSubscription) {
            log.debug("Transaction has subscriptionId set, allowing walletFrom and walletTo to be null");
            return;
        }

        switch (type) {
            case EXPENSE:
                if (transactionDto.getWalletToId() != null) {
                    log.debug("Forcing walletTo to null for EXPENSE transaction");
                    transactionDto.setWalletToId(null);
                }
                if (transactionDto.getWalletFromId() == null) {
                    throw new IllegalArgumentException("EXPENSE transactions require walletFrom to be set (unless subscriptionId is provided)");
                }
                break;

            case INCOME:
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

    private void setRelatedEntities(Transaction transaction, TransactionDto transactionDto) {
        String username = transactionDto.getUsername();

        if (transactionDto.getWalletFromId() != null) {
            Wallet walletFrom = walletRepository.findById(transactionDto.getWalletFromId())
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Source wallet not found or access denied"));
            transaction.setWalletFrom(walletFrom);
        } else {
            transaction.setWalletFrom(null);
        }

        if (transactionDto.getWalletToId() != null) {
            Wallet walletTo = walletRepository.findById(transactionDto.getWalletToId())
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found or access denied"));
            transaction.setWalletTo(walletTo);
        } else {
            transaction.setWalletTo(null);
        }

        if (transactionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(transactionDto.getCategoryId())
                    .filter(cat -> cat.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
            transaction.setCategory(category);
        }

        if (transactionDto.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(transactionDto.getSubscriptionId())
                    .filter(sub -> sub.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));
            transaction.setSubscription(subscription);
        }
    }

    private void setExchangeRate(Transaction transaction) {
        Wallet walletFrom = transaction.getWalletFrom();
        Wallet walletTo = transaction.getWalletTo();

        if (walletFrom == null || walletTo == null ||
                walletFrom.getCurrency().equals(walletTo.getCurrency())) {
            transaction.setExchangeRate(BigDecimal.ONE);
        }
    }
}


