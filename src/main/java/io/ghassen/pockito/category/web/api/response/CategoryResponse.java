package io.ghassen.pockito.category.web.api.response;

import io.ghassen.pockito.domain.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private String id;

    private String username;

    private String name;

    private String color;

    private CategoryType categoryType;

    private String iconUrl;

    private String parentCategoryId;

    private String parentCategoryName;

    private Instant createdAt;

    private Instant updatedAt;

    private Boolean active;

    private Integer childCount;
}


