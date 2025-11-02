package io.ghassen.pockito.category.infrastructure.persistence.repository;

import io.ghassen.pockito.domain.Category;
import io.ghassen.pockito.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUserUsernameOrderByNameAsc(String username);

    List<Category> findByUserUsernameAndCategoryTypeOrderByNameAsc(String username, CategoryType categoryType);

    List<Category> findByUserUsernameAndParentCategoryIsNullOrderByNameAsc(String username);

    List<Category> findByParentCategoryIdOrderByNameAsc(String parentCategoryId);

    List<Category> findByUserUsernameAndParentCategoryIdOrderByNameAsc(String username, String parentCategoryId);

    boolean existsByUserUsernameAndName(String username, String name);

    Optional<Category> findByUserUsernameAndName(String username, String name);

    long countByUserUsername(String username);

    long countByUserUsernameAndCategoryType(String username, CategoryType categoryType);

    List<Category> findByUserUsernameAndColorOrderByNameAsc(String username, String color);

    List<Category> findByUserUsernameAndParentCategoryId(String username, String parentCategoryId);

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


