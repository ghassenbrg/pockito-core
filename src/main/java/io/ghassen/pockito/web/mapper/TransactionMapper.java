package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.web.types.dto.TransactionDto;
import io.ghassen.pockito.web.types.request.TransactionRequest;
import io.ghassen.pockito.web.types.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting between Transaction entity and TransactionDto.
 * 
 * Uses MapStruct to generate efficient mapping implementations.
 * Handles the conversion between domain entities and DTOs for the web layer.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface TransactionMapper {

    /**
     * Convert Transaction entity to TransactionDto.
     * 
     * @param transaction the transaction entity to convert
     * @return the corresponding DTO
     */
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "walletFrom.id", target = "walletFromId")
    @Mapping(source = "walletTo.id", target = "walletToId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(source = "subscription.name", target = "subscriptionName")
    @Mapping(source = "walletFrom.name", target = "walletFromName")
    @Mapping(source = "walletTo.name", target = "walletToName")
    @Mapping(expression = "java(transaction.getWalletFrom() != null ? transaction.getWalletFrom().getCurrency() : (transaction.getSubscription() != null ? transaction.getSubscription().getCurrency() : null))", target = "walletFromCurrency")
    @Mapping(source = "walletTo.currency", target = "walletToCurrency")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(expression = "java(transaction.getSubscription() != null ? transaction.getSubscription().getIconUrl() : (transaction.getCategory() != null ? transaction.getCategory().getIconUrl() : null))", target = "iconUrl")
    @Mapping(expression = "java(transaction.getWalletToAmount())", target = "walletToAmount")
    TransactionDto toDto(Transaction transaction);

    /**
     * Convert TransactionDto to Transaction entity.
     * 
     * @param transactionDto the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object needs to be set separately
    @Mapping(target = "walletFrom", ignore = true) // Wallet objects need to be set separately
    @Mapping(target = "walletTo", ignore = true)
    @Mapping(target = "category", ignore = true) // Category object needs to be set separately
    @Mapping(target = "subscription", ignore = true) // Subscription object needs to be set separately
    @Mapping(target = "id", ignore = true) // ID is managed by the system
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Transaction toEntity(TransactionDto transactionDto);

    /**
     * Update existing Transaction entity with data from TransactionDto.
     * 
     * @param transactionDto the DTO containing update data
     * @param transaction the existing entity to update
     * @return the updated entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object should not be changed
    @Mapping(target = "walletFrom", ignore = true) // Wallet objects need to be set separately
    @Mapping(target = "walletTo", ignore = true)
    @Mapping(target = "category", ignore = true) // Category object needs to be set separately
    @Mapping(target = "subscription", ignore = true) // Subscription object needs to be set separately
    @Mapping(target = "id", ignore = true) // ID should not be changed
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Transaction updateEntityFromDto(TransactionDto transactionDto, @MappingTarget Transaction transaction);

    /**
     * Convert list of Transaction entities to list of TransactionDto.
     * 
     * @param transactions the list of transaction entities to convert
     * @return the corresponding list of DTOs
     */
    List<TransactionDto> toDtoList(List<Transaction> transactions);

    /**
     * Convert list of TransactionDto to list of Transaction entities.
     * 
     * @param transactionDtos the list of DTOs to convert
     * @return the corresponding list of entities
     */
    List<Transaction> toEntityList(List<TransactionDto> transactionDtos);

    /**
     * Convert TransactionRequest to TransactionDto.
     * 
     * @param transactionRequest the request to convert
     * @return the corresponding DTO
     */
    TransactionDto requestToDto(TransactionRequest transactionRequest);

    /**
     * Convert TransactionDto to TransactionResponse.
     * 
     * @param transactionDto the DTO to convert
     * @return the corresponding response
     */
    TransactionResponse dtoToResponse(TransactionDto transactionDto);

    /**
     * Convert list of TransactionDto to list of TransactionResponse.
     * 
     * @param transactionDtos the list of DTOs to convert
     * @return the corresponding list of responses
     */
    List<TransactionResponse> dtoListToResponseList(List<TransactionDto> transactionDtos);
}
