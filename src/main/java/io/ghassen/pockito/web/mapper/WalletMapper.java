package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.web.types.dto.WalletDto;
import io.ghassen.pockito.web.types.request.WalletRequest;
import io.ghassen.pockito.web.types.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting between Wallet entity and WalletDto.
 * 
 * Uses MapStruct to generate efficient mapping implementations.
 * Handles the conversion between domain entities and DTOs for the web layer.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface WalletMapper {

    /**
     * Convert Wallet entity to WalletDto.
     * 
     * @param wallet the wallet entity to convert
     * @return the corresponding DTO
     */
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "initialBalance", target = "balance")
    @Mapping(expression = "java(wallet.getArchivedAt() == null)", target = "active")
    WalletDto toDto(Wallet wallet);

    /**
     * Convert WalletDto to Wallet entity.
     * 
     * @param walletDto the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object needs to be set separately
    @Mapping(target = "id", ignore = true) // ID is managed by the system
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Wallet toEntity(WalletDto walletDto);

    /**
     * Update existing Wallet entity with data from WalletDto.
     * 
     * @param walletDto the DTO containing update data
     * @param wallet the existing entity to update
     * @return the updated entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object should not be changed
    @Mapping(target = "id", ignore = true) // ID should not be changed
    @Mapping(target = "currency", ignore = true) // Currency should not be changed
    @Mapping(target = "isDefault", ignore = true) // isDefault is managed by setDefaultWalletForUser
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Wallet updateEntityFromDto(WalletDto walletDto, @MappingTarget Wallet wallet);

    /**
     * Convert list of Wallet entities to list of WalletDto.
     * 
     * @param wallets the list of wallet entities to convert
     * @return the corresponding list of DTOs
     */
    List<WalletDto> toDtoList(List<Wallet> wallets);

    /**
     * Convert list of WalletDto to list of Wallet entities.
     * 
     * @param walletDtos the list of DTOs to convert
     * @return the corresponding list of entities
     */
    List<Wallet> toEntityList(List<WalletDto> walletDtos);

    /**
     * Convert WalletRequest to WalletDto.
     * 
     * @param walletRequest the request to convert
     * @return the corresponding DTO
     */
    WalletDto requestToDto(WalletRequest walletRequest);

    /**
     * Convert WalletDto to WalletResponse.
     * 
     * @param walletDto the DTO to convert
     * @return the corresponding response
     */
    WalletResponse dtoToResponse(WalletDto walletDto);

    /**
     * Convert list of WalletDto to list of WalletResponse.
     * 
     * @param walletDtos the list of DTOs to convert
     * @return the corresponding list of responses
     */
    List<WalletResponse> dtoListToResponseList(List<WalletDto> walletDtos);
}
