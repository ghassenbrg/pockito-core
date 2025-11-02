package io.ghassen.pockito.wallet.web.mapper;

import io.ghassen.pockito.wallet.application.dto.WalletDto;
import io.ghassen.pockito.wallet.web.api.request.WalletRequest;
import io.ghassen.pockito.wallet.web.api.response.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletApiMapper {
    WalletDto requestToDto(WalletRequest walletRequest);
    WalletResponse dtoToResponse(WalletDto walletDto);
    List<WalletResponse> dtoListToResponseList(List<WalletDto> walletDtos);
}


