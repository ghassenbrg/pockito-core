package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
  
  // Find all categories for a user, ordered by name
  List<Category> findByUserIdOrderByNameAsc(String userId);
  
  // Find active categories for a user, ordered by name
  @Query("SELECT c FROM Category c WHERE c.userId = :userId AND c.archivedAt IS NULL ORDER BY c.name ASC")
  List<Category> findActiveByUserId(@Param("userId") String userId);
  
  // Find categories by type for a user, ordered by name
  @Query("SELECT c FROM Category c WHERE c.userId = :userId AND c.type = :type ORDER BY c.name ASC")
  List<Category> findByUserIdAndType(String userId, @Param("type") Category.CategoryType type);
  
  // Find active categories by type for a user, ordered by name
  @Query("SELECT c FROM Category c WHERE c.userId = :userId AND c.type = :type AND c.archivedAt IS NULL ORDER BY c.name ASC")
  List<Category> findActiveByUserIdAndType(String userId, @Param("type") Category.CategoryType type);
  
  // Find by ID and user ID
  Optional<Category> findByIdAndUserId(UUID id, String userId);
  
  // Check if name exists for user and type (active only)
  @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.userId = :userId AND c.type = :type AND LOWER(c.name) = LOWER(:name) AND c.archivedAt IS NULL")
  boolean existsActiveByUserIdAndTypeAndNameIgnoreCase(
      @Param("userId") String userId, 
      @Param("type") Category.CategoryType type, 
      @Param("name") String name);
  
  // Check if name exists for user and type (excluding current category for updates)
  @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.userId = :userId AND c.type = :type AND LOWER(c.name) = LOWER(:name) AND c.archivedAt IS NULL AND c.id != :excludeId")
  boolean existsActiveByUserIdAndTypeAndNameIgnoreCaseExcludingId(
      @Param("userId") String userId, 
      @Param("type") Category.CategoryType type, 
      @Param("name") String name,
      @Param("excludeId") UUID excludeId);
  
  // Search categories by name for a user
  @Query("SELECT c FROM Category c WHERE c.userId = :userId AND c.name LIKE %:searchTerm% ORDER BY c.name ASC")
  List<Category> searchByUserIdAndTerm(String userId, @Param("searchTerm") String searchTerm);
  
  // Search active categories by name for a user
  @Query("SELECT c FROM Category c WHERE c.userId = :userId AND c.name LIKE %:searchTerm% AND c.archivedAt IS NULL ORDER BY c.name ASC")
  List<Category> searchActiveByUserIdAndTerm(String userId, @Param("searchTerm") String searchTerm);
  
  // Find categories by parent ID
  @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name ASC")
  List<Category> findByParentIdOrderByNameAsc(@Param("parentId") UUID parentId);
  
  // Find active categories by parent ID
  @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.archivedAt IS NULL ORDER BY c.name ASC")
  List<Category> findActiveByParentId(@Param("parentId") UUID parentId);
}
