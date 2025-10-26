package io.ghassen.pockito.web.types.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for lists of categories.
 * 
 * Used to wrap category lists in API responses instead of returning List directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResponse {

    /**
     * List of categories.
     */
    private List<CategoryResponse> categories;

    /**
     * Total count of categories (useful for pagination).
     */
    private Long totalCount;
}
