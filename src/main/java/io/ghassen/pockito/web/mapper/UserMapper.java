package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.web.types.dto.UserDto;
import io.ghassen.pockito.web.types.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper interface for User entity and UserDto.
 * 
 * This interface will automatically generate the mapping implementation at compile time.
 * MapStruct provides type-safe mapping with excellent performance.
 * 
 * @see <a href="https://mapstruct.org/">MapStruct Documentation</a>
 */
@Mapper(
    componentModel = "spring", // Makes the mapper available as a Spring bean
    unmappedTargetPolicy = ReportingPolicy.IGNORE, // Ignores unmapped fields in target
    unmappedSourcePolicy = ReportingPolicy.IGNORE  // Ignores unmapped fields in source
)
public interface UserMapper {

    /**
     * Maps User entity to UserDto.
     * 
     * @param user the User entity to map
     * @return the mapped UserDto
     */
    @Mapping(target = "username", source = "username")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "defaultCurrency", source = "defaultCurrency")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserDto toDto(User user);

    /**
     * Maps UserDto to User entity.
     * 
     * @param userDto the UserDto to map
     * @return the mapped User entity
     */
    @Mapping(target = "username", source = "username")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "defaultCurrency", source = "defaultCurrency")
    @Mapping(target = "createdAt", ignore = true) // Ignore audit fields when mapping from DTO
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "archivedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "systemAction", ignore = true)
    User toEntity(UserDto userDto);

    /**
     * Updates an existing User entity with data from UserDto.
     * 
     * @param userDto the UserDto containing update data
     * @param user the existing User entity to update
     * @return the updated User entity
     */
    @Mapping(target = "username", ignore = true) // Never update username (immutable)
    @Mapping(target = "createdAt", ignore = true) // Never update audit fields
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "archivedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "systemAction", ignore = true)
    void updateEntityFromDto(UserDto userDto, @org.mapstruct.MappingTarget User user);

    /**
     * Maps UserDto to UserResponse.
     * 
     * @param userDto the UserDto to map
     * @return the mapped UserResponse
     */
    UserResponse toResponse(UserDto userDto);
}
