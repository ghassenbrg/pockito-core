package io.ghassen.pockito.transaction.web.mapper;

import io.ghassen.pockito.transaction.application.dto.TransactionDto;
import io.ghassen.pockito.transaction.web.api.request.TransactionRequest;
import io.ghassen.pockito.transaction.web.api.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionApiMapper {
    TransactionDto requestToDto(TransactionRequest request);
    TransactionResponse dtoToResponse(TransactionDto dto);
    List<TransactionResponse> dtoListToResponseList(List<TransactionDto> dtos);
}


