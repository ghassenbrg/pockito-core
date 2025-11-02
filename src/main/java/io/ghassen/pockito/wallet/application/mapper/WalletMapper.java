package io.ghassen.pockito.wallet.application.mapper;

import io.ghassen.pockito.domain.Wallet;
import io.ghassen.pockito.wallet.application.dto.WalletDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface WalletMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "initialBalance", target = "balance")
    @Mapping(expression = "java(wallet.getArchivedAt() == null)", target = "active")
    WalletDto toDto(Wallet wallet);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Wallet toEntity(WalletDto walletDto);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Wallet updateEntityFromDto(WalletDto walletDto, @MappingTarget Wallet wallet);

    List<WalletDto> toDtoList(List<Wallet> wallets);

    List<Wallet> toEntityList(List<WalletDto> walletDtos);
}


