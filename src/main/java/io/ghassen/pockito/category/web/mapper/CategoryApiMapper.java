package io.ghassen.pockito.category.web.mapper;

import io.ghassen.pockito.category.application.dto.CategoryDto;
import io.ghassen.pockito.category.web.api.request.CategoryRequest;
import io.ghassen.pockito.category.web.api.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryApiMapper {
    CategoryDto requestToDto(CategoryRequest request);
    CategoryResponse dtoToResponse(CategoryDto dto);
    List<CategoryResponse> dtoListToResponseList(List<CategoryDto> dtos);
}


