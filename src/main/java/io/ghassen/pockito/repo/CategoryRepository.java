package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category entity operations.
 * 
 * Provides standard CRUD operations and custom query methods for category management.
 * Extends JpaRepository to inherit common database operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find all categories belonging to a specific user.
     * 
     * @param username the username of the user
     * @return list of categories owned by the user
     */
    List<Category> findByUserUsernameOrderByNameAsc(String username);

    /**
     * Find all categories of a specific type belonging to a user.
     * 
     * @param username the username of the user
     * @param categoryType the category type to filter by
     * @return list of categories of the specified type owned by the user
     */
    List<Category> findByUserUsernameAndCategoryTypeOrderByNameAsc(String username, CategoryType categoryType);

    /**
     * Find all root categories (no parent) for a specific user.
     * 
     * @param username the username of the user
     * @return list of root categories owned by the user
     */
    List<Category> findByUserUsernameAndParentCategoryIsNullOrderByNameAsc(String username);

    /**
     * Find all child categories of a specific parent category.
     * 
     * @param parentCategoryId the parent category ID
     * @return list of child categories
     */
    List<Category> findByParentCategoryIdOrderByNameAsc(UUID parentCategoryId);

    /**
     * Find all child categories of a specific parent category for a user.
     * 
     * @param username the username of the user
     * @param parentCategoryId the parent category ID
     * @return list of child categories owned by the user
     */
    List<Category> findByUserUsernameAndParentCategoryIdOrderByNameAsc(String username, UUID parentCategoryId);

    /**
     * Check if a user has a category with a specific name.
     * 
     * @param username the username of the user
     * @param name the category name to check
     * @return true if a category with the given name exists for the user
     */
    boolean existsByUserUsernameAndName(String username, String name);

    /**
     * Find category by user and name.
     * 
     * @param username the username of the user
     * @param name the category name
     * @return optional containing the category if it exists
     */
    Optional<Category> findByUserUsernameAndName(String username, String name);

    /**
     * Count categories belonging to a specific user.
     * 
     * @param username the username of the user
     * @return the number of categories owned by the user
     */
    long countByUserUsername(String username);

    /**
     * Count categories of a specific type belonging to a user.
     * 
     * @param username the username of the user
     * @param categoryType the category type to count
     * @return the number of categories of the specified type owned by the user
     */
    long countByUserUsernameAndCategoryType(String username, CategoryType categoryType);

    /**
     * Find categories by color for a specific user.
     * 
     * @param username the username of the user
     * @param color the color to filter by
     * @return list of categories with the specified color
     */
    List<Category> findByUserUsernameAndColorOrderByNameAsc(String username, String color);

    /**
     * Find categories with a specific parent category for a user.
     * Used to check if a category can be deleted (no children).
     * 
     * @param username the username of the user
     * @param parentCategoryId the parent category ID
     * @return list of categories with the specified parent
     */
    List<Category> findByUserUsernameAndParentCategoryId(String username, UUID parentCategoryId);

    /**
     * Find all categories in a hierarchical tree for a user.
     * Returns categories ordered by hierarchy (parents first, then children).
     * 
     * @param username the username of the user
     * @return list of categories in hierarchical order
     */
    @Query("""
      SELECT c
      FROM Category c
      LEFT JOIN c.parentCategory pc
      WHERE c.user.username = :username
      ORDER BY
        CASE WHEN pc IS NULL THEN 0 ELSE 1 END,
        pc.name,
        c.name
    """)
    List<Category> findHierarchicalCategoriesByUserUsername(@Param("username") String username);

    /**
     * Find all categories of a specific type in hierarchical order for a user.
     * 
     * @param username the username of the user
     * @param categoryType the category type to filter by
     * @return list of categories of the specified type in hierarchical order
     */
    @Query("""
       SELECT c
       FROM Category c
       LEFT JOIN c.parentCategory pc
       WHERE c.user.username = :username
         AND c.categoryType = :categoryType
       ORDER BY
         CASE WHEN pc IS NULL THEN 0 ELSE 1 END,
         pc.name,
         c.name
     """)
    List<Category> findHierarchicalCategoriesByUserUsernameAndType(
        @Param("username") String username, 
        @Param("categoryType") CategoryType categoryType);
}
