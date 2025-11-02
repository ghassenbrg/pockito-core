package io.ghassen.pockito.transaction.web.controller;

import io.ghassen.pockito.domain.enums.TransactionType;
import io.ghassen.pockito.domain.validation.ValidationGroups;
import io.ghassen.pockito.transaction.application.service.TransactionService;
import io.ghassen.pockito.transaction.application.dto.TransactionDto;
import io.ghassen.pockito.transaction.web.api.request.TransactionRequest;
import io.ghassen.pockito.transaction.web.api.response.TransactionResponse;
import io.ghassen.pockito.transaction.web.api.response.TransactionListResponse;
import io.ghassen.pockito.transaction.web.mapper.TransactionApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "APIs for managing financial transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionApiMapper transactionMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new transaction with validation rules based on transaction type. " +
                     "EXPENSE requires walletFrom, INCOME requires walletTo, TRANSFER requires at least one wallet."
    )
    public ResponseEntity<TransactionResponse> createTransaction(
            @Validated(ValidationGroups.Create.class) @RequestBody TransactionRequest transactionRequest) {
        
        log.info("Creating transaction of type: {}", transactionRequest.getTransactionType());
        
        TransactionDto transactionDto = transactionMapper.requestToDto(transactionRequest);
        
        TransactionDto createdTransactionDto = transactionService.createTransaction(transactionDto);
        
        TransactionResponse transactionResponse = transactionMapper.dtoToResponse(createdTransactionDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }

    @PutMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update an existing transaction",
        description = "Updates an existing transaction with the same validation rules as creation."
    )
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Transaction ID to update") 
            @PathVariable String transactionId,
            @Validated(ValidationGroups.Update.class) @RequestBody TransactionRequest transactionRequest) {
        
        log.info("Updating transaction with ID: {}", transactionId);
        
        TransactionDto transactionDto = transactionMapper.requestToDto(transactionRequest);
        
        try {
            TransactionDto updatedTransactionDto = transactionService.updateTransaction(transactionId, transactionDto);
            
            TransactionResponse transactionResponse = transactionMapper.dtoToResponse(updatedTransactionDto);
            
            return ResponseEntity.ok(transactionResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieves a single transaction by its unique ID."
    )
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction ID to retrieve") 
            @PathVariable String transactionId) {
        
        log.debug("Getting transaction with ID: {}", transactionId);
        
        return transactionService.getTransactionById(transactionId)
            .map(transactionMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "List transactions by criteria",
        description = "Retrieves transactions with filtering by userId, walletId, effectiveDate range, " +
                     "and transactionType. Supports pagination for large transaction volumes."
    )
    public ResponseEntity<Page<TransactionDto>> listTransactions(
            @Parameter(description = "Wallet ID to filter by") 
            @RequestParam(required = false) String walletId,
            @Parameter(description = "Start date for date range filtering (yyyy-MM-dd)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for date range filtering (yyyy-MM-dd)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Transaction type to filter by") 
            @RequestParam(required = false) TransactionType transactionType,
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
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

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all user transactions",
        description = "Retrieves all transactions for the authenticated user without pagination. " +
                     "Use with caution for users with large transaction volumes."
    )
    public ResponseEntity<TransactionListResponse> getAllTransactions() {
        log.debug("Getting all transactions for user");
        
        List<TransactionDto> transactionDtos = transactionService.getAllUserTransactions();
        
        List<TransactionResponse> transactionResponses = transactionMapper.dtoListToResponseList(transactionDtos);
        
        TransactionListResponse response = TransactionListResponse.builder()
            .transactions(transactionResponses)
            .totalCount((long) transactionResponses.size())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transactions by wallet",
        description = "Retrieves all transactions involving a specific wallet with pagination."
    )
    public ResponseEntity<Page<TransactionDto>> getTransactionsByWallet(
            @Parameter(description = "Wallet ID to filter by") 
            @PathVariable String walletId,
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
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
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
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

    @GetMapping("/type/{transactionType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get transactions by type",
        description = "Retrieves transactions of a specific type (EXPENSE, INCOME, TRANSFER) with pagination."
    )
    public ResponseEntity<Page<TransactionDto>> getTransactionsByType(
            @Parameter(description = "Transaction type to filter by") 
            @PathVariable TransactionType transactionType,
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
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


