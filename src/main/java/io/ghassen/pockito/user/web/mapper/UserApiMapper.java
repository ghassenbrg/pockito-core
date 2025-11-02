package io.ghassen.pockito.user.web.mapper;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.user.application.dto.UserDto;
import io.ghassen.pockito.user.web.api.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserApiMapper {
    UserDto toDto(User user);
    UserResponse toResponse(UserDto userDto);
}


