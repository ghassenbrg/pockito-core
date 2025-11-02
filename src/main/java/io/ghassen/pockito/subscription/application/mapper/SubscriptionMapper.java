package io.ghassen.pockito.subscription.application.mapper;

import io.ghassen.pockito.domain.Subscription;
import io.ghassen.pockito.subscription.application.dto.SubscriptionDto;
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
public interface SubscriptionMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "categoryId.id", target = "categoryId")
    @Mapping(source = "categoryId.name", target = "categoryName")
    @Mapping(source = "defaultWalletId.id", target = "defaultWalletId")
    @Mapping(source = "defaultWalletId.name", target = "defaultWalletName")
    @Mapping(target = "monthlyEquivalentAmount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    SubscriptionDto toDto(Subscription subscription);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "defaultWalletId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Subscription toEntity(SubscriptionDto subscriptionDto);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "defaultWalletId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nextDueDate", ignore = true)
    @Mapping(target = "lastPaymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Subscription updateEntityFromDto(SubscriptionDto subscriptionDto, @MappingTarget Subscription subscription);

    List<SubscriptionDto> toDtoList(List<Subscription> subscriptions);

    List<Subscription> toEntityList(List<SubscriptionDto> subscriptionDtos);
}


