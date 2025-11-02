package io.ghassen.pockito.user.application.mapper;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.user.application.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "username", source = "username")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "defaultCurrency", source = "defaultCurrency")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserDto toDto(User user);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "defaultCurrency", source = "defaultCurrency")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "archivedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "systemAction", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "archivedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "systemAction", ignore = true)
    void updateEntityFromDto(UserDto userDto, @org.mapstruct.MappingTarget User user);
}


