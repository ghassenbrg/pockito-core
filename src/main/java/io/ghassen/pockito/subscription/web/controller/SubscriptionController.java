package io.ghassen.pockito.subscription.web.controller;

import io.ghassen.pockito.domain.validation.ValidationGroups;
import io.ghassen.pockito.subscription.application.service.SubscriptionService;
import io.ghassen.pockito.subscription.web.mapper.SubscriptionApiMapper;
import io.ghassen.pockito.subscription.application.dto.SubscriptionDto;
import io.ghassen.pockito.subscription.web.api.request.PaySubscriptionRequest;
import io.ghassen.pockito.subscription.web.api.request.SubscriptionRequest;
import io.ghassen.pockito.subscription.web.api.response.SubscriptionResponse;
import io.ghassen.pockito.transaction.application.dto.TransactionDto;
import io.ghassen.pockito.transaction.web.api.response.TransactionResponse;
import io.ghassen.pockito.transaction.web.mapper.TransactionApiMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionApiMapper subscriptionMapper;
    private final TransactionApiMapper transactionMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create a new subscription",
        description = "Creates a new subscription for the authenticated user."
    )
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Validated(ValidationGroups.Create.class) @RequestBody SubscriptionRequest subscriptionRequest) {
        
        log.info("Creating subscription: {}", subscriptionRequest.getName());
        
        SubscriptionDto subscriptionDto = subscriptionMapper.requestToDto(subscriptionRequest);
        
        SubscriptionDto createdSubscriptionDto = subscriptionService.createSubscription(subscriptionDto);
        
        SubscriptionResponse subscriptionResponse = subscriptionMapper.dtoToResponse(createdSubscriptionDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionResponse);
    }

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
        
        SubscriptionDto subscriptionDto = subscriptionMapper.requestToDto(subscriptionRequest);
        
        try {
            SubscriptionDto updatedSubscriptionDto = subscriptionService.updateSubscription(subscriptionId, subscriptionDto);
            
            SubscriptionResponse subscriptionResponse = subscriptionMapper.dtoToResponse(updatedSubscriptionDto);
            
            return ResponseEntity.ok(subscriptionResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update subscription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all subscriptions",
        description = "Retrieves all subscriptions for the authenticated user, ordered by name."
    )
    public ResponseEntity<List<SubscriptionResponse>> listSubscriptions() {
        
        log.debug("Getting all subscriptions for authenticated user");
        
        List<SubscriptionDto> subscriptionDtos = subscriptionService.listSubscriptions();
        
        List<SubscriptionResponse> subscriptionResponses = subscriptionMapper.dtoListToResponseList(subscriptionDtos);
        
        return ResponseEntity.ok(subscriptionResponses);
    }

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

    @PostMapping("/{subscriptionId}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Pay subscription",
        description = "Processes a payment for a subscription. Creates a new EXPENSE transaction and updates the subscription's nextDueDate. " +
                "Exchange rate is only used if subscription currency differs from wallet currency, otherwise it's ignored. " +
                "If skip is true, skips payment and transaction creation, only updates nextDueDate and lastPaymentDate."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment Skipped successfully"),
        @ApiResponse(responseCode = "200", description = "Payment processed successfully and transaction created")
    })
    public ResponseEntity<TransactionResponse> paySubscription(
            @Parameter(description = "Subscription ID to pay") 
            @PathVariable String subscriptionId,
            @RequestBody(required = false) PaySubscriptionRequest payRequest) {
        
        log.info("Processing payment for subscription ID: {}", subscriptionId);
        
        String walletId = payRequest != null ? payRequest.getWalletId() : null;
        java.math.BigDecimal exchangeRate = payRequest != null ? payRequest.getExchangeRate() : null;
        Boolean skip = payRequest != null && payRequest.getSkip() != null ? payRequest.getSkip() : false;
        
        try {
            TransactionDto transactionDto = subscriptionService.paySubscription(subscriptionId, walletId, exchangeRate, skip);
            
            if (skip && transactionDto == null) {
                return ResponseEntity.noContent().build();
            }
            
            TransactionResponse transactionResponse = transactionMapper.dtoToResponse(transactionDto);
            
            return ResponseEntity.ok(transactionResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to pay subscription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}


