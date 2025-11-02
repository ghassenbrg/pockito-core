package io.ghassen.pockito.web;

import io.ghassen.pockito.service.SubscriptionService;
import io.ghassen.pockito.web.mapper.SubscriptionMapper;
import io.ghassen.pockito.web.mapper.TransactionMapper;
import io.ghassen.pockito.web.types.dto.SubscriptionDto;
import io.ghassen.pockito.web.types.dto.TransactionDto;
import io.ghassen.pockito.web.types.request.PaySubscriptionRequest;
import io.ghassen.pockito.web.types.request.SubscriptionRequest;
import io.ghassen.pockito.web.types.response.SubscriptionResponse;
import io.ghassen.pockito.web.types.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import io.ghassen.pockito.web.validation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for subscription operations.
 * 
 * Provides HTTP endpoints for subscription CRUD operations and payment processing.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    private final TransactionMapper transactionMapper;

    /**
     * Create a new subscription for the authenticated user.
     * 
     * @param subscriptionRequest the subscription data to create
     * @return the created subscription
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new subscription",
        description = "Creates a new subscription for the authenticated user."
    )
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Validated(ValidationGroups.Create.class) @RequestBody SubscriptionRequest subscriptionRequest) {
        
        log.info("Creating subscription: {}", subscriptionRequest.getName());
        
        // Convert request to DTO for service layer
        SubscriptionDto subscriptionDto = subscriptionMapper.requestToDto(subscriptionRequest);
        
        SubscriptionDto createdSubscriptionDto = subscriptionService.createSubscription(subscriptionDto);
        
        // Convert DTO to response
        SubscriptionResponse subscriptionResponse = subscriptionMapper.dtoToResponse(createdSubscriptionDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionResponse);
    }

    /**
     * Update an existing subscription for the authenticated user.
     * 
     * @param subscriptionId the subscription ID to update
     * @param subscriptionRequest the updated subscription data
     * @return the updated subscription
     */
    @PutMapping("/{subscriptionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update subscription",
        description = "Updates an existing subscription for the authenticated user."
    )
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @Parameter(description = "Subscription ID to update") 
            @PathVariable String subscriptionId,
            @Validated(ValidationGroups.Update.class) @RequestBody SubscriptionRequest subscriptionRequest) {
        
        log.info("Updating subscription with ID: {}", subscriptionId);
        
        // Convert request to DTO for service layer
        SubscriptionDto subscriptionDto = subscriptionMapper.requestToDto(subscriptionRequest);
        
        try {
            SubscriptionDto updatedSubscriptionDto = subscriptionService.updateSubscription(subscriptionId, subscriptionDto);
            
            // Convert DTO to response
            SubscriptionResponse subscriptionResponse = subscriptionMapper.dtoToResponse(updatedSubscriptionDto);
            
            return ResponseEntity.ok(subscriptionResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update subscription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get a specific subscription by ID for the authenticated user.
     * 
     * @param subscriptionId the subscription ID
     * @return the subscription if found and owned by user
     */
    @GetMapping("/{subscriptionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get subscription by ID",
        description = "Retrieves a specific subscription by its ID for the authenticated user."
    )
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @Parameter(description = "Subscription ID") 
            @PathVariable String subscriptionId) {
        
        log.debug("Getting subscription with ID: {}", subscriptionId);
        
        return subscriptionService.getSubscription(subscriptionId)
            .map(subscriptionMapper::dtoToResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all subscriptions for the authenticated user.
     * 
     * @return list of user's subscriptions
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all subscriptions",
        description = "Retrieves all subscriptions for the authenticated user, ordered by name."
    )
    public ResponseEntity<List<SubscriptionResponse>> listSubscriptions() {
        
        log.debug("Getting all subscriptions for authenticated user");
        
        List<SubscriptionDto> subscriptionDtos = subscriptionService.listSubscriptions();
        
        // Convert DTOs to responses
        List<SubscriptionResponse> subscriptionResponses = subscriptionMapper.dtoListToResponseList(subscriptionDtos);
        
        return ResponseEntity.ok(subscriptionResponses);
    }

    /**
     * Delete a subscription for the authenticated user.
     * 
     * @param subscriptionId the subscription ID to delete
     * @return no content on success
     */
    @DeleteMapping("/{subscriptionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete subscription",
        description = "Deletes a subscription for the authenticated user."
    )
    public ResponseEntity<Void> deleteSubscription(
            @Parameter(description = "Subscription ID to delete") 
            @PathVariable String subscriptionId) {
        
        log.info("Deleting subscription with ID: {}", subscriptionId);
        
        try {
            subscriptionService.deleteSubscription(subscriptionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete subscription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Process a payment for a subscription.
     * 
     * Creates a new EXPENSE transaction and updates the subscription's nextDueDate.
     * 
     * @param subscriptionId the subscription ID
     * @param payRequest the payment request containing the wallet ID and exchange rate (optional)
     * @return the created transaction
     */
    @PostMapping("/{subscriptionId}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Pay subscription",
        description = "Processes a payment for a subscription. Creates a new EXPENSE transaction and updates the subscription's nextDueDate. " +
                "Exchange rate is only used if subscription currency differs from wallet currency, otherwise it's ignored."
    )
    public ResponseEntity<TransactionResponse> paySubscription(
            @Parameter(description = "Subscription ID to pay") 
            @PathVariable String subscriptionId,
            @RequestBody(required = false) PaySubscriptionRequest payRequest) {
        
        log.info("Processing payment for subscription ID: {}", subscriptionId);
        
        String walletId = payRequest != null ? payRequest.getWalletId() : null;
        java.math.BigDecimal exchangeRate = payRequest != null ? payRequest.getExchangeRate() : null;
        
        try {
            TransactionDto transactionDto = subscriptionService.paySubscription(subscriptionId, walletId, exchangeRate);
            
            // Convert DTO to response
            TransactionResponse transactionResponse = transactionMapper.dtoToResponse(transactionDto);
            
            return ResponseEntity.ok(transactionResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to pay subscription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

