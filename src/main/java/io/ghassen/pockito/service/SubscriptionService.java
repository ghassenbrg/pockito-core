package io.ghassen.pockito.service;

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
import io.ghassen.pockito.repo.CategoryRepository;
import io.ghassen.pockito.repo.SubscriptionRepository;
import io.ghassen.pockito.repo.TransactionRepository;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.repo.WalletRepository;
import io.ghassen.pockito.security.SecurityUtils;
import io.ghassen.pockito.web.mapper.SubscriptionMapper;
import io.ghassen.pockito.web.mapper.TransactionMapper;
import io.ghassen.pockito.web.types.dto.SubscriptionDto;
import io.ghassen.pockito.web.types.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for subscription business operations.
 * 
 * Provides business logic for subscription management including CRUD
 * operations,
 * payment processing, and subscription scheduling.
 */
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

    /**
     * Create a new subscription for the authenticated user.
     * 
     * @param subscriptionDto the subscription data to create
     * @return the created subscription DTO
     * @throws IllegalArgumentException if user not found or validation fails
     */
    public SubscriptionDto createSubscription(SubscriptionDto subscriptionDto) {
        // Automatically set username from authenticated user
        String username = SecurityUtils.getCurrentUserId();
        subscriptionDto.setUsername(username);

        log.debug("Creating subscription for user: {}", username);

        // Validate user exists
        User user = userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Convert to entity and save
        Subscription subscription = subscriptionMapper.toEntity(subscriptionDto);
        subscription.setId(null);
        subscription.setUser(user);

        // Set category
        Category category = categoryRepository.findById(subscriptionDto.getCategoryId())
                .filter(cat -> cat.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
        subscription.setCategoryId(category);

        // Set default wallet
        Wallet defaultWallet = walletRepository.findById(subscriptionDto.getDefaultWalletId())
                .filter(wallet -> wallet.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Default wallet not found or access denied"));
        subscription.setDefaultWalletId(defaultWallet);

        // Always calculate nextDueDate when creating a new subscription
        // However, if endDate is set and is before or equal to the calculated
        // nextDueDate, set nextDueDate to null
        LocalDate calculatedNextDueDate = calculateNextDueDate(
                subscriptionDto.getStartDate(),
                subscriptionDto.getFrequency(),
                subscriptionDto.getInterval(),
                subscriptionDto.getDayOfMonth(),
                subscriptionDto.getDayOfWeek(),
                subscriptionDto.getMonthOfYear());

        // Check if endDate is set to a value before the calculated nextDueDate
        if (subscriptionDto.getEndDate() != null &&
                subscriptionDto.getEndDate().isBefore(calculatedNextDueDate)) {
            subscription.setNextDueDate(null);
            subscriptionDto.setNextDueDate(null);
            log.debug("endDate {} is before calculated nextDueDate {}, setting nextDueDate to null on creation",
                    subscriptionDto.getEndDate(), calculatedNextDueDate);
        } else if (subscriptionDto.getNextDueDate() != null) {
            // If explicitly provided in DTO, use it
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

    /**
     * Update an existing subscription for the authenticated user.
     * 
     * @param subscriptionId  the subscription ID to update
     * @param subscriptionDto the updated subscription data
     * @return the updated subscription DTO
     * @throws IllegalArgumentException if subscription not found, not owned by
     *                                  user, or validation fails
     */
    public SubscriptionDto updateSubscription(String subscriptionId, SubscriptionDto subscriptionDto) {
        // Automatically set username from authenticated user
        String username = SecurityUtils.getCurrentUserId();
        subscriptionDto.setUsername(username);

        log.debug("Updating subscription with ID: {} for user: {}", subscriptionId, username);

        Subscription existingSubscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        // Update entity with new data
        subscriptionMapper.updateEntityFromDto(subscriptionDto, existingSubscription);

        // Handle category change
        if (subscriptionDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(subscriptionDto.getCategoryId())
                    .filter(cat -> cat.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found or access denied"));
            existingSubscription.setCategoryId(category);
        }

        // Handle default wallet change
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

        // If endDate exists and the calculated nextDueDate is after or equal to
        // endDate, set nextDueDate to null
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

    /**
     * Get a subscription by ID for the authenticated user.
     * 
     * @param subscriptionId the subscription ID
     * @return the subscription DTO if found and owned by user
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionDto> getSubscription(String subscriptionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting subscription with ID: {} for user: {}", subscriptionId, username);

        Optional<SubscriptionDto> subscriptionDto = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .map(subscriptionMapper::toDto);

        if (subscriptionDto.isPresent()) {
            // Set derived fields for the subscription
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

    /**
     * Get all subscriptions for the authenticated user.
     * 
     * @return list of subscription DTOs ordered by name
     */
    @Transactional(readOnly = true)
    public List<SubscriptionDto> listSubscriptions() {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Getting subscriptions for user: {}", username);
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameOrderByNameAsc(username);
        List<SubscriptionDto> subscriptionDtos = subscriptionMapper.toDtoList(subscriptions);

        // Set derived fields for each subscription
        for (int i = 0; i < subscriptions.size(); i++) {
            setDerivedFields(subscriptionDtos.get(i), subscriptions.get(i));
        }

        log.info("Retrieved {} subscriptions for user: {}", subscriptionDtos.size(), username);
        return subscriptionDtos;
    }

    /**
     * Delete a subscription for the authenticated user.
     * 
     * If the subscription has transactions:
     * - If transactions are linked to wallets, unlink them from the subscription and set their note to the subscription name if null
     * - If transactions are NOT linked to any wallet, delete them
     * 
     * @param subscriptionId the subscription ID to delete
     * @throws IllegalArgumentException if subscription not found or not owned by
     *                                  user
     */
    public void deleteSubscription(String subscriptionId) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Deleting subscription with ID: {} for user: {}", subscriptionId, username);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        // Get all transactions linked to this subscription
        List<Transaction> linkedTransactions = transactionRepository.findBySubscriptionId(subscriptionId);
        
        if (!linkedTransactions.isEmpty()) {
            log.debug("Found {} transactions linked to subscription {}", linkedTransactions.size(), subscriptionId);
            
            for (Transaction transaction : linkedTransactions) {
                // Check if transaction is linked to any wallet
                boolean hasWalletLink = transaction.getWalletFrom() != null || transaction.getWalletTo() != null;
                
                if (hasWalletLink) {
                    // Unlink from subscription and update note if null
                    transaction.setSubscription(null);
                    if (transaction.getNote() == null || transaction.getNote().isEmpty()) {
                        transaction.setNote(subscription.getName());
                        log.debug("Set note to subscription name '{}' for transaction {}", subscription.getName(), transaction.getId());
                    }
                    transactionRepository.save(transaction);
                    log.debug("Unlinked transaction {} from subscription", transaction.getId());
                } else {
                    // Delete transaction since it's not linked to any wallet
                    transactionRepository.delete(transaction);
                    log.debug("Deleted transaction {} as it's not linked to any wallet", transaction.getId());
                }
            }
        }

        subscriptionRepository.delete(subscription);
        log.info("Deleted subscription with ID: {} for user: {}", subscriptionId, username);
    }

    /**
     * Process a payment for a subscription.
     * 
     * Creates a new EXPENSE transaction and updates the subscription's nextDueDate.
     * 
     * @param subscriptionId the subscription ID
     * @param walletId       the wallet ID to charge (overrides defaultWalletId)
     * @param exchangeRate   the exchange rate to use (only if subscription currency
     *                       differs from wallet currency)
     * @return the created transaction DTO
     * @throws IllegalArgumentException if subscription not found, not owned by
     *                                  user, or wallet not found
     */
    public TransactionDto paySubscription(String subscriptionId, String walletId, BigDecimal exchangeRate) {
        String username = SecurityUtils.getCurrentUserId();
        log.debug("Processing payment for subscription ID: {} with wallet ID: {} and exchange rate: {} for user: {}",
                subscriptionId, walletId, exchangeRate, username);

        // Get subscription and verify ownership
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found or access denied"));

        if (!subscription.getEnabled()) {
            throw new IllegalArgumentException("Cannot pay for disabled subscription");
        }

        // Determine wallet to charge
        Wallet wallet = null;
        if (walletId != null) {
            wallet = walletRepository.findById(walletId)
                    .filter(w -> w.getUser().getUsername().equals(username))
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found or access denied"));
        }
        /* Uncomment this when we have a way to set the default wallet for a subscription
        else {
            wallet = subscription.getDefaultWalletId();
            // Verify default wallet still exists and belongs to user
            if (wallet == null || !wallet.getUser().getUsername().equals(username)) {
                throw new IllegalArgumentException("Default wallet not found or access denied");
            }
        }
        */

        // Calculate transaction amount based on exchange rate if currencies differ
        BigDecimal transactionAmount;

        if (wallet != null && !subscription.getCurrency().equals(wallet.getCurrency())) {
            // Currencies differ, use provided exchange rate to calculate amount
            if (exchangeRate != null) {
                // Calculate amount in wallet currency: subscription amount * exchange rate
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
            // Currencies match, use subscription amount as-is
            transactionAmount = subscription.getAmount();
            log.debug("Currencies match, using subscription amount: {} {}",
                    subscription.getAmount(), subscription.getCurrency());
        }

        // Create transaction
        // Since walletTo is null (EXPENSE transaction), exchange rate is always 1.0
        Transaction transaction = Transaction.builder()
                .user(subscription.getUser())
                .transactionType(TransactionType.EXPENSE)
                .walletFrom(wallet)
                .walletTo(null)
                .amount(transactionAmount)
                .exchangeRate(BigDecimal.ONE) // Always 1.0 since there's no walletTo
                .category(subscription.getCategoryId())
                .subscription(subscription)
                .effectiveDate(LocalDate.now())
                .note(subscription.getNote())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction with ID: {} for subscription ID: {}", savedTransaction.getId(), subscriptionId);

        // Set lastPaymentDate to nextDueDate when payment is successful and transaction
        // is created
        LocalDate paymentDate = subscription.getNextDueDate();
        subscription.setLastPaymentDate(paymentDate);

        // Update subscription's nextDueDate
        // Use lastPaymentDate as base date if it's set (not null), otherwise use
        // startDate
        // Since we just set lastPaymentDate to paymentDate, we use paymentDate as the
        // base
        LocalDate baseDate = subscription.getLastPaymentDate() != null
                ? subscription.getLastPaymentDate()
                : subscription.getStartDate();

        // Always recalculate nextDueDate when payment is made
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

    /**
     * Calculate the next due date based on frequency, interval, and optional
     * scheduling fields.
     * 
     * @param baseDate    the base date to calculate from
     * @param frequency   the frequency unit
     * @param interval    the interval value
     * @param dayOfMonth  optional day of month (1-31) for MONTHLY frequency
     * @param dayOfWeek   optional day of week (1-7) for WEEKLY frequency
     * @param monthOfYear optional month of year (1-12) for YEARLY frequency
     * @return the calculated next due date
     */
    private LocalDate calculateNextDueDate(LocalDate baseDate, SubscriptionFrequency frequency,
            Integer interval, Integer dayOfMonth, DayOfWeek dayOfWeek, MonthOfYear monthOfYear) {
        LocalDate nextDueDate = baseDate;

        switch (frequency) {
            case DAILY:
                nextDueDate = baseDate.plusDays(interval);
                break;

            case WEEKLY:
                if (dayOfWeek != null) {
                    // Find the next occurrence of the specified day of week
                    // Convert java.time.DayOfWeek (Monday=1, Sunday=7) to our enum value
                    java.time.DayOfWeek baseDayOfWeek = baseDate.getDayOfWeek();
                    int baseDayValue = baseDayOfWeek.getValue(); // java.time.DayOfWeek also uses Mon=1, Sun=7
                    int targetDayValue = dayOfWeek.getValue();
                    int daysUntilTarget = (targetDayValue - baseDayValue + 7) % 7;
                    if (daysUntilTarget == 0) {
                        daysUntilTarget = 7; // If today is the target day, move to next week
                    }
                    nextDueDate = baseDate.plusDays(daysUntilTarget + (interval - 1) * 7);
                } else {
                    nextDueDate = baseDate.plusWeeks(interval);
                }
                break;

            case MONTHLY:
                if (dayOfMonth != null) {
                    // Find the next occurrence of the specified day of month
                    nextDueDate = baseDate;
                    int targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                    nextDueDate = nextDueDate.withDayOfMonth(targetDay);

                    // If the calculated date is before the base date, move to next interval
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
                    // Find the next occurrence of the specified month and day
                    nextDueDate = baseDate;
                    nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());
                    int targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                    nextDueDate = nextDueDate.withDayOfMonth(targetDay);

                    // If the calculated date is before the base date, move to next interval
                    if (nextDueDate.isBefore(baseDate) || nextDueDate.isEqual(baseDate)) {
                        nextDueDate = nextDueDate.plusYears(interval);
                        nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());
                        targetDay = Math.min(dayOfMonth, nextDueDate.lengthOfMonth());
                        nextDueDate = nextDueDate.withDayOfMonth(targetDay);
                    }
                } else if (monthOfYear != null) {
                    nextDueDate = baseDate.plusYears(interval);
                    nextDueDate = nextDueDate.withMonth(monthOfYear.getValue());

                    // If the calculated date is before the base date, move to next interval
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

    /**
     * Calculate the monthly equivalent amount for a subscription.
     * 
     * @param amount    the subscription amount
     * @param frequency the frequency unit
     * @param interval  the interval value
     * @return the monthly equivalent amount
     */
    private BigDecimal calculateMonthlyEquivalentAmount(BigDecimal amount, SubscriptionFrequency frequency,
            Integer interval) {
        BigDecimal monthlyEquivalent;

        switch (frequency) {
            case DAILY:
                // Convert daily to monthly: amount * 30 / interval
                monthlyEquivalent = amount.multiply(BigDecimal.valueOf(30)).divide(BigDecimal.valueOf(interval), 2,
                        RoundingMode.HALF_UP);
                break;

            case WEEKLY:
                // Convert weekly to monthly: amount * 4.33 / interval (average weeks per month)
                monthlyEquivalent = amount.multiply(BigDecimal.valueOf(4.33)).divide(BigDecimal.valueOf(interval), 2,
                        RoundingMode.HALF_UP);
                break;

            case MONTHLY:
                // Already monthly: amount / interval
                monthlyEquivalent = amount.divide(BigDecimal.valueOf(interval), 2, RoundingMode.HALF_UP);
                break;

            case YEARLY:
                // Convert yearly to monthly: amount / 12 / interval
                monthlyEquivalent = amount.divide(BigDecimal.valueOf(12 * interval), 2, RoundingMode.HALF_UP);
                break;

            default:
                monthlyEquivalent = BigDecimal.ZERO;
        }

        return monthlyEquivalent;
    }

    /**
     * Set derived fields for a subscription DTO.
     * 
     * @param subscriptionDto the subscription DTO to set derived fields for
     * @param subscription    the subscription entity to get information from
     */
    private void setDerivedFields(SubscriptionDto subscriptionDto, Subscription subscription) {
        // Set category name
        if (subscriptionDto.getCategoryName() == null && subscription.getCategoryId() != null) {
            subscriptionDto.setCategoryName(subscription.getCategoryId().getName());
        }

        // Set default wallet name
        if (subscriptionDto.getDefaultWalletName() == null && subscription.getDefaultWalletId() != null) {
            subscriptionDto.setDefaultWalletName(subscription.getDefaultWalletId().getName());
        }

        // Calculate monthly equivalent amount
        if (subscriptionDto.getMonthlyEquivalentAmount() == null) {
            BigDecimal monthlyEquivalent = calculateMonthlyEquivalentAmount(
                    subscription.getAmount(),
                    subscription.getFrequency(),
                    subscription.getInterval());
            subscriptionDto.setMonthlyEquivalentAmount(monthlyEquivalent);
        }

        // Calculate isActive based on enabled (DB value), start date, and end date
        LocalDate today = LocalDate.now();
        Boolean calculatedIsActive = subscription.getEnabled() != null && subscription.getEnabled()
                && subscription.getStartDate() != null
                && !subscription.getStartDate().isAfter(today)
                && (subscription.getEndDate() == null || !subscription.getEndDate().isBefore(today));
        subscriptionDto.setIsActive(calculatedIsActive);
    }
}
