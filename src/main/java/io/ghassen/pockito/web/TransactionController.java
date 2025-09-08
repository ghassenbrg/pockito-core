package io.ghassen.pockito.web;

import io.ghassen.pockito.domain.TransactionType;
import io.ghassen.pockito.service.TransactionService;
import io.ghassen.pockito.web.dto.TransactionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for transaction operations.
 * 
 * Provides HTTP endpoints for transaction CRUD operations and management.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Create a new transaction for the authenticated user.
     * 
     * @param transactionDto the transaction data to create
     * @return the created transaction
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new transaction with validation rules based on transaction type. " +
                     "EXPENSE requires walletFrom, INCOME requires walletTo, TRANSFER requires at least one wallet."
    )
    public ResponseEntity<TransactionDto> createTransaction(
            @Valid @RequestBody TransactionDto transactionDto) {
        
        log.info("Creating transaction of type: {}", transactionDto.getTransactionType());
        
        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * Update an existing transaction for the authenticated user.
     * 
     * @param transactionId the transaction ID to update
     * @param transactionDto the updated transaction data
     * @return the updated transaction
     */
    @PutMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update an existing transaction",
        description = "Updates an existing transaction with the same validation rules as creation."
    )
    public ResponseEntity<TransactionDto> updateTransaction(
            @Parameter(description = "Transaction ID to update") 
            @PathVariable UUID transactionId,
            @Valid @RequestBody TransactionDto transactionDto) {
        
        log.info("Updating transaction with ID: {}", transactionId);
        
        try {
            TransactionDto updatedTransaction = transactionService.updateTransaction(transactionId, transactionDto);
            return ResponseEntity.ok(updatedTransaction);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get a specific transaction by ID for the authenticated user.
     * 
     * @param transactionId the transaction ID
     * @return the transaction if found and owned by user
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a single transaction by its unique ID."
    )
    public ResponseEntity<TransactionDto> getTransaction(
            @Parameter(description = "Transaction ID to retrieve") 
            @PathVariable UUID transactionId) {
        
        log.debug("Getting transaction with ID: {}", transactionId);
        
        return transactionService.getTransactionById(transactionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a transaction for the authenticated user.
     * 
     * @param transactionId the transaction ID to delete
     * @return no content on success
     */
    @DeleteMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete a transaction",
        description = "Removes a transaction by ID."
    )
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID to delete") 
            @PathVariable UUID transactionId) {
        
        log.info("Deleting transaction with ID: {}", transactionId);
        
        try {
            transactionService.deleteTransaction(transactionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * List transactions by criteria with pagination for the authenticated user.
     * 
     * @param walletId optional wallet ID to filter by
     * @param startDate optional start date for date range filtering
     * @param endDate optional end date for date range filtering
     * @param transactionType optional transaction type to filter by
     * @param pageable pagination information
     * @return page of transactions matching the criteria
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "List transactions by criteria",
        description = "Retrieves transactions with filtering by userId, walletId, effectiveDate range, " +
                     "and transactionType. Supports pagination for large transaction volumes."
    )
    public ResponseEntity<Page<TransactionDto>> listTransactions(
            @Parameter(description = "Wallet ID to filter by") 
            @RequestParam(required = false) UUID walletId,
            @Parameter(description = "Start date for date range filtering (yyyy-MM-dd)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for date range filtering (yyyy-MM-dd)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Transaction type to filter by") 
            @RequestParam(required = false) TransactionType transactionType,
            @PageableDefault(size = 20, sort = "effectiveDate") Pageable pageable) {
        
        log.debug("Listing transactions with filters - walletId: {}, startDate: {}, endDate: {}, type: {}", 
                walletId, startDate, endDate, transactionType);
        
        try {
            Page<TransactionDto> transactions = transactionService.getTransactionsByCriteria(
                    walletId, startDate, endDate, transactionType, pageable);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to list transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all transactions for the authenticated user (without pagination).
     * 
     * @return list of all transactions for the user
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all user transactions",
        description = "Retrieves all transactions for the authenticated user without pagination. " +
                     "Use with caution for users with large transaction volumes."
    )
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        log.debug("Getting all transactions for user");
        
        List<TransactionDto> transactions = transactionService.getAllUserTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions for a specific wallet with pagination.
     * 
     * @param walletId the wallet ID to filter by
     * @param pageable pagination information
     * @return page of transactions involving the specified wallet
     */
    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transactions by wallet",
        description = "Retrieves all transactions involving a specific wallet with pagination."
    )
    public ResponseEntity<Page<TransactionDto>> getTransactionsByWallet(
            @Parameter(description = "Wallet ID to filter by") 
            @PathVariable UUID walletId,
            @PageableDefault(size = 20, sort = "effectiveDate") Pageable pageable) {
        
        log.debug("Getting transactions for wallet: {}", walletId);
        
        try {
            Page<TransactionDto> transactions = transactionService.getTransactionsByCriteria(
                    walletId, null, null, null, pageable);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get transactions for wallet: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get transactions by date range with pagination.
     * 
     * @param startDate the start date for date range filtering
     * @param endDate the end date for date range filtering
     * @param pageable pagination information
     * @return page of transactions within the date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transactions by date range",
        description = "Retrieves transactions within a specific date range with pagination."
    )
    public ResponseEntity<Page<TransactionDto>> getTransactionsByDateRange(
            @Parameter(description = "Start date for date range filtering (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for date range filtering (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "effectiveDate") Pageable pageable) {
        
        log.debug("Getting transactions for date range: {} to {}", startDate, endDate);
        
        try {
            Page<TransactionDto> transactions = transactionService.getTransactionsByCriteria(
                    null, startDate, endDate, null, pageable);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get transactions by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get transactions by type with pagination.
     * 
     * @param transactionType the transaction type to filter by
     * @param pageable pagination information
     * @return page of transactions of the specified type
     */
    @GetMapping("/type/{transactionType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transactions by type",
        description = "Retrieves transactions of a specific type (EXPENSE, INCOME, TRANSFER) with pagination."
    )
    public ResponseEntity<Page<TransactionDto>> getTransactionsByType(
            @Parameter(description = "Transaction type to filter by") 
            @PathVariable TransactionType transactionType,
            @PageableDefault(size = 20, sort = "effectiveDate") Pageable pageable) {
        
        log.debug("Getting transactions of type: {}", transactionType);
        
        try {
            Page<TransactionDto> transactions = transactionService.getTransactionsByCriteria(
                    null, null, null, transactionType, pageable);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get transactions by type: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
