package io.ghassen.pockito.category.web.api.request;

import io.ghassen.pockito.domain.enums.CategoryType;
import io.ghassen.pockito.domain.validation.CategoryId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    @NotBlank(message = "Color is required")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code (e.g., #A1B2C3)")
    @Size(min = 7, max = 7, message = "Color must be exactly 7 characters including the # symbol")
    private String color;

    @NotNull(message = "Category type is required")
    private CategoryType categoryType;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    @CategoryId
    private String parentCategoryId;
}


