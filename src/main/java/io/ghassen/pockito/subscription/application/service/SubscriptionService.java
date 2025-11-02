package io.ghassen.pockito.subscription.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.Subscription;
import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.category.infrastructure.persistence.repository.CategoryRepository;
import io.ghassen.pockito.subscription.infrastructure.persistence.repository.SubscriptionRepository;
import io.ghassen.pockito.transaction.infrastructure.persistence.repository.TransactionRepository;
import io.ghassen.pockito.user.infrastructure.persistence.repository.UserRepository;
import io.ghassen.pockito.wallet.infrastructure.persistence.repository.WalletRepository;
import io.ghassen.pockito.shared.security.SecurityUtils;
import io.ghassen.pockito.subscription.application.dto.SubscriptionDto;
import io.ghassen.pockito.subscription.application.mapper.SubscriptionMapper;
import io.ghassen.pockito.transaction.application.dto.TransactionDto;
import io.ghassen.pockito.transaction.application.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final TransactionMapper transactionMapper;

    public SubscriptionDto createSubscription(SubscriptionDto subscriptionDto) {
        String username = SecurityUtils.getCurrentUserId();
        subscriptionDto.setUsername(username);

        log.debug("Creating subscription for user: {}", username);

        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Subscription subscription = subscriptionMapper.toEntity(subscriptionDto);
        subscription.setId(null);
        subscription.setUser(user);

        Category category = categoryRepository.findById(subscriptionDto.getCategoryId())
                .filter(cat -> cat.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
        subscription.setCategoryId(category);

        Wallet defaultWallet = walletRepository.findById(subscriptionDto.getDefaultWalletId())
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Default wallet not found or access denied"));
        subscription.setDefaultWalletId(defaultWallet);

        LocalDate calculatedNextDueDate = calculateNextDueDate(
                subscriptionDto.getStartDate(),
                subscriptionDto.getFrequency(),
                subscriptionDto.getInterval(),
                subscriptionDto.getDayOfMonth(),
                subscriptionDto.getDayOfWeek(),
                subscriptionDto.getMonthOfYear());

        if (subscriptionDto.getEndDate() != null &&
                subscriptionDto.getEndDate().isBefore(calculatedNextDueDate)) {
            subscription.setNextDueDate(null);
            subscriptionDto.setNextDueDate(null);
            log.debug("endDate {} is before calculated nextDueDate {}, setting nextDueDate to null on creation",
                    subscriptionDto.getEndDate(), calculatedNextDueDate);
        } else if (subscriptionDto.getNextDueDate() != null) {
            subscription.setNextDueDate(subscriptionDto.getNextDueDate());
        } else {
            subscription.setNextDueDate(calculatedNextDueDate);
            subscriptionDto.setNextDueDate(calculatedNextDueDate);
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Created subscription with ID: {} for user: {}", savedSubscription.getId(), username);

        SubscriptionDto createdSubscriptionDto = subscriptionMapper.toDto(savedSubscription);
        setDerivedFields(createdSubscriptionDto, savedSubscription);
        return createdSubscriptionDto;
    }

    public SubscriptionDto updateSubscription(String subscriptionId, SubscriptionDto subscriptionDto) {
        String username = SecurityUtils.getCurrentUserId();
        subscriptionDto.setUsername(username);

        log.debug("Updating subscription with ID: {} for user: {}", subscriptionId, username);

        Subscription existingSubscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        subscriptionMapper.updateEntityFromDto(subscriptionDto, existingSubscription);

        if (subscriptionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(subscriptionDto.getCategoryId())
                    .filter(cat -> cat.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
            existingSubscription.setCategoryId(category);
        }

        if (subscriptionDto.getDefaultWalletId() != null) {
            Wallet defaultWallet = walletRepository.findById(subscriptionDto.getDefaultWalletId())
                    .filter(wallet -> wallet.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Default wallet not found or access denied"));
            existingSubscription.setDefaultWalletId(defaultWallet);
        }

        LocalDate baseDate = existingSubscription.getLastPaymentDate() != null
                && existingSubscription.getLastPaymentDate().isAfter(existingSubscription.getStartDate())
                        ? existingSubscription.getLastPaymentDate()
                        : existingSubscription.getStartDate();

        LocalDate nextDueDate = calculateNextDueDate(
                baseDate,
                existingSubscription.getFrequency(),
                existingSubscription.getInterval(),
                existingSubscription.getDayOfMonth(),
                existingSubscription.getDayOfWeek(),
                existingSubscription.getMonthOfYear());

        if (existingSubscription.getEndDate() != null &&
                nextDueDate.isAfter(existingSubscription.getEndDate())) {
            existingSubscription.setNextDueDate(null);
            log.debug("Calculated nextDueDate {} is before or equal to endDate {}, setting nextDueDate to null",
                    nextDueDate, existingSubscription.getEndDate());
        } else {
            existingSubscription.setNextDueDate(nextDueDate);
            log.debug("Calculated new nextDueDate: {}", nextDueDate);
        }

        Subscription updatedSubscription = subscriptionRepository.save(
                existingSubscription);
        log.info("Updated subscription with ID: {} for user: {}", subscriptionId, username);

        SubscriptionDto updatedSubscriptionDto = subscriptionMapper.toDto(updatedSubscription);

        setDerivedFields(updatedSubscriptionDto, updatedSubscription);
        return updatedSubscriptionDto;
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionDto> getSubscription(String subscriptionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting subscription with ID: {} for user: {}", subscriptionId, username);

        Optional<SubscriptionDto> subscriptionDto = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .map(subscriptionMapper::toDto);

        if (subscriptionDto.isPresent()) {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .filter(s -> s.getUser().getUsername().equals(username))
                    .orElse(null);
            if (subscription != null) {
                setDerivedFields(subscriptionDto.get(), subscription);
            }
            log.info("Retrieved subscription with ID: {} for user: {}", subscriptionId, username);
        } else {
            log.info("Subscription with ID: {} not found or access denied for user: {}", subscriptionId, username);
        }

        return subscriptionDto;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> listSubscriptions() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting subscriptions for user: {}", username);
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameOrderByNameAsc(username);
        List<SubscriptionDto> subscriptionDtos = subscriptionMapper.toDtoList(subscriptions);

        for (int i = 0; i < subscriptions.size(); i++) {
            setDerivedFields(subscriptionDtos.get(i), subscriptions.get(i));
        }

        log.info("Retrieved {} subscriptions for user: {}", subscriptionDtos.size(), username);
        return subscriptionDtos;
    }

    public void deleteSubscription(String subscriptionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting subscription with ID: {} for user: {}", subscriptionId, username);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        List<Transaction> linkedTransactions = transactionRepository.findBySubscriptionId(subscriptionId);
        
        if (!linkedTransactions.isEmpty()) {
            log.debug("Found {} transactions linked to subscription {}", linkedTransactions.size(), subscriptionId);
            
            for (Transaction transaction : linkedTransactions) {
                boolean hasWalletLink = transaction.getWalletFrom() != null || transaction.getWalletTo() != null;
                
                if (hasWalletLink) {
                    transaction.setSubscription(null);
                    if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                        transaction.setNote(subscription.getName());
                        log.debug("Set note to subscription name '{}' for transaction {}", subscription.getName(), transaction.getId());
                    }
                    transactionRepository.save(transaction);
                    log.debug("Unlinked transaction {} from subscription", transaction.getId());
                } else {
                    transactionRepository.delete(transaction);
                    log.debug("Deleted transaction {} as it's not linked to any wallet", transaction.getId());
                }
            }
        }

        subscriptionRepository.delete(subscription);
        log.info("Deleted subscription with ID: {} for user: {}", subscriptionId, username);
    }

    public TransactionDto paySubscription(String subscriptionId, String walletId, BigDecimal exchangeRate, Boolean skip) {
        String username = SecurityUtils.getCurrentUserId();
        boolean shouldSkip = skip != null && skip;
        log.debug("Processing payment for subscription ID: {} with wallet ID: {} and exchange rate: {} for user: {}, skip: {}",
                subscriptionId, walletId, exchangeRate, username, shouldSkip);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        if (!subscription.getEnabled()) {
            throw new IllegalArgumentException("Cannot pay for disabled subscription");
        }

        if (shouldSkip) {
            log.info("Skipping payment for subscription ID: {} - only updating dates", subscriptionId);
            
            LocalDate paymentDate = subscription.getNextDueDate();
            subscription.setLastPaymentDate(paymentDate);
            
            LocalDate baseDate = subscription.getLastPaymentDate() != null
                    ? subscription.getLastPaymentDate()
                    : subscription.getStartDate();

            LocalDate nextDueDate = calculateNextDueDate(
                    baseDate,
                    subscription.getFrequency(),
                    subscription.getInterval(),
                    subscription.getDayOfMonth(),
                    subscription.getDayOfWeek(),
                    subscription.getMonthOfYear());
            subscription.setNextDueDate(nextDueDate);

            subscriptionRepository.save(subscription);
            log.info(
                    "Skipped payment - Updated subscription nextDueDate to: {} (calculated from base date: {}) and lastPaymentDate to: {} for subscription ID: {}",
                    nextDueDate, baseDate, paymentDate, subscriptionId);

            return null;
        }

        Wallet wallet = null;
        if (walletId != null) {
            wallet = walletRepository.findById(walletId)
                    .filter(w -> w.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));
        }

        BigDecimal transactionAmount;

        if (wallet != null && !subscription.getCurrency().equals(wallet.getCurrency())) {
            if (exchangeRate != null) {
                transactionAmount = subscription.getAmount().multiply(exchangeRate)
                        .setScale(2, RoundingMode.HALF_UP);
                log.debug(
                        "Using provided exchange rate: {} to calculate transaction amount: {} {} (subscription amount: {} {})",
                        exchangeRate, transactionAmount, wallet.getCurrency(),
                        subscription.getAmount(), subscription.getCurrency());
            } else {
                transactionAmount = subscription.getAmount();
                log.warn(
                        "Currencies differ but no exchange rate provided. Using subscription amount as-is (subscription: {} {}, wallet: {})",
                        subscription.getAmount(), subscription.getCurrency(), wallet.getCurrency());
            }
        } else {
            transactionAmount = subscription.getAmount();
            log.debug("Currencies match, using subscription amount: {} {}",
                    subscription.getAmount(), subscription.getCurrency());
        }

        Transaction transaction = Transaction.builder()
                .user(subscription.getUser())
                .transactionType(TransactionType.EXPENSE)
                .walletFrom(wallet)
                .walletTo(null)
                .amount(transactionAmount)
                .exchangeRate(BigDecimal.ONE)
                .category(subscription.getCategoryId())
                .subscription(subscription)
                .effectiveDate(LocalDate.now())
                .note(subscription.getNote())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction with ID: {} for subscription ID: {}", savedTransaction.getId(), subscriptionId);

        LocalDate paymentDate = subscription.getNextDueDate();
        subscription.setLastPaymentDate(paymentDate);

        LocalDate baseDate = subscription.getLastPaymentDate() != null
                ? subscription.getLastPaymentDate()
                : subscription.getStartDate();

        LocalDate nextDueDate = calculateNextDueDate(
                baseDate,
                subscription.getFrequency(),
                subscription.getInterval(),
                subscription.getDayOfMonth(),
                subscription.getDayOfWeek(),
                subscription.getMonthOfYear());
        subscription.setNextDueDate(nextDueDate);

        subscriptionRepository.save(subscription);
        log.info(
                "Updated subscription nextDueDate to: {} (calculated from base date: {}) and lastPaymentDate to: {} for subscription ID: {}",
                nextDueDate, baseDate, paymentDate, subscriptionId);

        return transactionMapper.toDto(savedTransaction);
    }

    private LocalDate calculateNextDueDate(LocalDate baseDate, SubscriptionFrequency frequency,
            Integer interval, Integer dayOfMonth, DayOfWeek dayOfWeek, MonthOfYear monthOfYear) {
        LocalDate nextDueDate = baseDate;

        switch (frequency) {
            case DAILY:
                nextDueDate = baseDate.plusDays(interval);
                break;

            case WEEKLY:
                if (dayOfWeek != null) {
                    java.time.DayOfWeek baseDayOfWeek = baseDate.getDayOfWeek();
                    int baseDayValue = baseDayOfWeek.getValue();
                    int targetDayValue = dayOfWeek.getValue();
                    int daysUntilTarget = (targetDayValue - baseDayValue + 7) % 7;
                    if (daysUntilTarget == 0) {
                        daysUntilTarget = 7;
                    }
                    nextDueDate = baseDate.plusDays(daysUntilTarget + (interval - 1) * 7);
                } else {
                    nextDueDate = baseDate.plusWeeks(interval);
                }
                break;

            case MONTHLY:
                if (dayOfMonth != null) {
                    nextDueDate = baseDate;
                    int targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                    nextDueDate = nextDueDate.withDayOfMonth(targetDay);

                    if (nextDueDate.isBefore(baseDate) || nextDueDate.isEqual(baseDate)) {
                        nextDueDate = nextDueDate.plusMonths(interval);
                        targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                        nextDueDate = nextDueDate.withDayOfMonth(targetDay);
                    }
                } else {
                    nextDueDate = baseDate.plusMonths(interval);
                }
                break;

            case YEARLY:
                if (monthOfYear != null && dayOfMonth != null) {
                    nextDueDate = baseDate;
                    nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());
                    int targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                    nextDueDate = nextDueDate.withDayOfMonth(targetDay);

                    if (nextDueDate.isBefore(baseDate) || nextDueDate.isEqual(baseDate)) {
                        nextDueDate = nextDueDate.plusYears(interval);
                        nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());
                        targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                        nextDueDate = nextDueDate.withDayOfMonth(targetDay);
                    }
                } else if (monthOfYear != null) {
                    nextDueDate = baseDate.plusYears(interval);
                    nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());

                    if (nextDueDate.isBefore(baseDate) || nextDueDate.isEqual(baseDate)) {
                        nextDueDate = nextDueDate.plusYears(interval);
                    }
                } else {
                    nextDueDate = baseDate.plusYears(interval);
                }
                break;
        }

        return nextDueDate;
    }

    private BigDecimal calculateMonthlyEquivalentAmount(BigDecimal amount, SubscriptionFrequency frequency,
            Integer interval) {
        BigDecimal monthlyEquivalent;

        switch (frequency) {
            case DAILY:
                monthlyEquivalent = amount.multiply(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(interval), 2,
                        RoundingMode.HALF_UP);
                break;

            case WEEKLY:
                monthlyEquivalent = amount.multiply(BigDecimal.valueOf(4.33)).divide(BigDecimal.valueOf(interval), 2,
                        RoundingMode.HALF_UP);
                break;

            case MONTHLY:
                monthlyEquivalent = amount.divide(BigDecimal.valueOf(interval), 2, RoundingMode.HALF_UP);
                break;

            case YEARLY:
                monthlyEquivalent = amount.divide(BigDecimal.valueOf(12 * interval), 2, RoundingMode.HALF_UP);
                break;

            default:
                monthlyEquivalent = BigDecimal.ZERO;
        }

        return monthlyEquivalent;
    }

    private void setDerivedFields(SubscriptionDto subscriptionDto, Subscription subscription) {
        if (subscriptionDto.getCategoryName() == null && subscription.getCategoryId() != null) {
            subscriptionDto.setCategoryName(subscription.getCategoryId().getName());
        }

        if (subscriptionDto.getDefaultWalletName() == null && subscription.getDefaultWalletId() != null) {
            subscriptionDto.setDefaultWalletName(subscription.getDefaultWalletId().getName());
        }

        if (subscriptionDto.getMonthlyEquivalentAmount() == null) {
            BigDecimal monthlyEquivalent = calculateMonthlyEquivalentAmount(
                    subscription.getAmount(),
                    subscription.getFrequency(),
                    subscription.getInterval());
            subscriptionDto.setMonthlyEquivalentAmount(monthlyEquivalent);
        }

        LocalDate today = LocalDate.now();
        Boolean calculatedIsActive = subscription.getEnabled() != null && subscription.getEnabled()
                && subscription.getStartDate() != null
                && !subscription.getStartDate().isAfter(today)
                && (subscription.getEndDate() == null || !subscription.getEndDate().isBefore(today));
        subscriptionDto.setIsActive(calculatedIsActive);
    }
}


