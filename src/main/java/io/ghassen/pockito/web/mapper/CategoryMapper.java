package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.web.types.dto.CategoryDto;
import io.ghassen.pockito.web.types.request.CategoryRequest;
import io.ghassen.pockito.web.types.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper interface for converting between Category entity and CategoryDto.
 * 
 * Uses MapStruct to generate efficient mapping implementations.
 * Handles the conversion between domain entities and DTOs for the web layer.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface CategoryMapper {

    /**
     * Convert Category entity to CategoryDto.
     * 
     * @param category the category entity to convert
     * @return the corresponding DTO
     */
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    @Mapping(expression = "java(category.getArchivedAt() == null)", target = "active")
    @Mapping(target = "childCount", ignore = true) // Will be set by service

    CategoryDto toDto(Category category);

    /**
     * Convert CategoryDto to Category entity.
     * 
     * @param categoryDto the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object needs to be set separately
    @Mapping(target = "parentCategory", ignore = true) // Parent category needs to be set separately
    @Mapping(target = "id", ignore = true) // ID is managed by the system
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    /**
     * Update existing Category entity with data from CategoryDto.
     * 
     * @param categoryDto the DTO containing update data
     * @param category the existing entity to update
     * @return the updated entity
     */
    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true) // User object should not be changed
    @Mapping(target = "parentCategory", ignore = true) // Parent category needs to be handled separately
    @Mapping(target = "id", ignore = true) // ID should not be changed
    @Mapping(target = "createdAt", ignore = true) // Audit fields are managed by the system
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Category updateEntityFromDto(CategoryDto categoryDto, @MappingTarget Category category);

    /**
     * Convert list of Category entities to list of CategoryDto.
     * 
     * @param categories the list of category entities to convert
     * @return the corresponding list of DTOs
     */
    List<CategoryDto> toDtoList(List<Category> categories);

    /**
     * Convert list of CategoryDto to list of Category entities.
     * 
     * @param categoryDtos the list of DTOs to convert
     * @return the corresponding list of entities
     */
    List<Category> toEntityList(List<CategoryDto> categoryDtos);

    /**
     * Convert CategoryRequest to CategoryDto.
     * 
     * @param categoryRequest the request to convert
     * @return the corresponding DTO
     */
    CategoryDto requestToDto(CategoryRequest categoryRequest);

    /**
     * Convert CategoryDto to CategoryResponse.
     * 
     * @param categoryDto the DTO to convert
     * @return the corresponding response
     */
    CategoryResponse dtoToResponse(CategoryDto categoryDto);

    /**
     * Convert list of CategoryDto to list of CategoryResponse.
     * 
     * @param categoryDtos the list of DTOs to convert
     * @return the corresponding list of responses
     */
    List<CategoryResponse> dtoListToResponseList(List<CategoryDto> categoryDtos);
}
