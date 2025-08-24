package io.ghassen.pockito.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.Wallet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class CategoryDtos {

    public record CreateReq(
        @NotBlank(message = "Category name is required")
        @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Category type is required")
        Category.CategoryType type,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex color code")
        String color,

        @NotNull(message = "Icon type is required")
        Wallet.IconType iconType,

        @NotBlank(message = "Icon value is required")
        @Size(max = 255, message = "Icon value must not exceed 255 characters")
        String iconValue,

        UUID parentId
    ) {}

    public record UpdateReq(
        @NotBlank(message = "Category name is required")
        @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Category type is required")
        Category.CategoryType type,

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex color code")
        String color,

        @NotNull(message = "Icon type is required")
        Wallet.IconType iconType,

        @NotBlank(message = "Icon value is required")
        @Size(max = 255, message = "Icon value must not exceed 255 characters")
        String iconValue,

        UUID parentId
    ) {}

    public record Resp(
        UUID id,
        String name,
        Category.CategoryType type,
        String color,
        Wallet.IconType iconType,
        String iconValue,
        UUID parentId,
        String parentName,
        @JsonProperty("isActive") boolean isActive,
        String userId,
        Instant createdAt,
        Instant updatedAt
    ) {}
}
