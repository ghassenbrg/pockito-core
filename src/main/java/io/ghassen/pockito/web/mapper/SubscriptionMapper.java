package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.Subscription;
import io.ghassen.pockito.web.types.dto.SubscriptionDto;
import io.ghassen.pockito.web.types.request.SubscriptionRequest;
import io.ghassen.pockito.web.types.response.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting between Subscription entity and SubscriptionDto.
 * 
 * Uses MapStruct to generate efficient mapping implementations.
 * Handles the conversion between domain entities and DTOs for the web layer.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface SubscriptionMapper {

    /**
     * Convert Subscription entity to SubscriptionDto.
     * 
     * @param subscription the subscription entity to convert
     * @return the corresponding DTO
     */
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "categoryId.id", target = "categoryId")
    @Mapping(source = "categoryId.name", target = "categoryName")
    @Mapping(source = "defaultWalletId.id", target = "defaultWalletId")
    @Mapping(source = "defaultWalletId.name", target = "defaultWalletName")
    @Mapping(target = "monthlyEquivalentAmount", ignore = true) // Will be calculated in service
    @Mapping(target = "isActive", ignore = true) // Will be calculated in service
    SubscriptionDto toDto(Subscription subscription);

    /**
     * Convert SubscriptionDto to Subscription entity.
     * 
     * @param subscriptionDto the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object needs to be set separately
    @Mapping(target = "categoryId", ignore = true) // Category needs to be set separately
    @Mapping(target = "defaultWalletId", ignore = true) // Wallet needs to be set separately
    @Mapping(target = "id", ignore = true) // ID is managed by the system
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Subscription toEntity(SubscriptionDto subscriptionDto);

    /**
     * Update existing Subscription entity with data from SubscriptionDto.
     * 
     * @param subscriptionDto the DTO containing update data
     * @param subscription the existing entity to update
     * @return the updated entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object should not be changed
    @Mapping(target = "categoryId", ignore = true) // Category needs to be handled separately
    @Mapping(target = "defaultWalletId", ignore = true) // Wallet needs to be handled separately
    @Mapping(target = "id", ignore = true) // ID should not be changed
    @Mapping(target = "nextDueDate", ignore = true) // Next due date is calculated by the service
    @Mapping(target = "lastPaymentDate", ignore = true) // Last payment date is calculated by the service
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Subscription updateEntityFromDto(SubscriptionDto subscriptionDto, @MappingTarget Subscription subscription);

    /**
     * Convert list of Subscription entities to list of SubscriptionDto.
     * 
     * @param subscriptions the list of subscription entities to convert
     * @return the corresponding list of DTOs
     */
    List<SubscriptionDto> toDtoList(List<Subscription> subscriptions);

    /**
     * Convert list of SubscriptionDto to list of Subscription entities.
     * 
     * @param subscriptionDtos the list of DTOs to convert
     * @return the corresponding list of entities
     */
    List<Subscription> toEntityList(List<SubscriptionDto> subscriptionDtos);

    /**
     * Convert SubscriptionRequest to SubscriptionDto.
     * 
     * @param subscriptionRequest the request to convert
     * @return the corresponding DTO
     */
    SubscriptionDto requestToDto(SubscriptionRequest subscriptionRequest);

    /**
     * Convert SubscriptionDto to SubscriptionResponse.
     * 
     * @param subscriptionDto the DTO to convert
     * @return the corresponding response
     */
    SubscriptionResponse dtoToResponse(SubscriptionDto subscriptionDto);

    /**
     * Convert list of SubscriptionDto to list of SubscriptionResponse.
     * 
     * @param subscriptionDtos the list of DTOs to convert
     * @return the corresponding list of responses
     */
    List<SubscriptionResponse> dtoListToResponseList(List<SubscriptionDto> subscriptionDtos);
}

