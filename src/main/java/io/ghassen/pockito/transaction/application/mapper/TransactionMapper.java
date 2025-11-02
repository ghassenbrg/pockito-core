package io.ghassen.pockito.transaction.application.mapper;

import io.ghassen.pockito.domain.Transaction;
import io.ghassen.pockito.transaction.application.dto.TransactionDto;
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
public interface TransactionMapper {

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

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "walletFrom", ignore = true)
    @Mapping(target = "walletTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Transaction toEntity(TransactionDto transactionDto);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "walletFrom", ignore = true)
    @Mapping(target = "walletTo", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Transaction updateEntityFromDto(TransactionDto transactionDto, @MappingTarget Transaction transaction);

    List<TransactionDto> toDtoList(List<Transaction> transactions);

    List<Transaction> toEntityList(List<TransactionDto> transactionDtos);
}


