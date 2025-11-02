package io.ghassen.pockito.category.application.mapper;

import io.ghassen.pockito.category.application.dto.CategoryDto;
import io.ghassen.pockito.domain.Category;
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
public interface CategoryMapper {

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "parentCategory.name", target = "parentCategoryName")
    @Mapping(expression = "java(category.getArchivedAt() == null)", target = "active")
    @Mapping(target = "childCount", ignore = true)
    CategoryDto toDto(Category category);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Category toEntity(CategoryDto categoryDto);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Category updateEntityFromDto(CategoryDto categoryDto, @MappingTarget Category category);

    List<CategoryDto> toDtoList(List<Category> categories);

    List<Category> toEntityList(List<CategoryDto> categoryDtos);
}


