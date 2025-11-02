package io.ghassen.pockito.subscription.web.mapper;

import io.ghassen.pockito.subscription.application.dto.SubscriptionDto;
import io.ghassen.pockito.subscription.web.api.request.SubscriptionRequest;
import io.ghassen.pockito.subscription.web.api.response.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionApiMapper {
    SubscriptionDto requestToDto(SubscriptionRequest request);
    SubscriptionResponse dtoToResponse(SubscriptionDto dto);
    List<SubscriptionResponse> dtoListToResponseList(List<SubscriptionDto> dtos);
}


