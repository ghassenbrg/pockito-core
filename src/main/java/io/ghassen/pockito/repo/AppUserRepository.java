package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {
  
  Optional<AppUser> findByIdAndArchivedAtIsNull(String id);
  
  Optional<AppUser> findByEmailAndArchivedAtIsNull(String email);
  
  boolean existsByIdAndArchivedAtIsNull(String id);
  
  boolean existsByEmailAndArchivedAtIsNull(String email);
  
  @Query("SELECT u FROM AppUser u WHERE u.id = :id AND u.archivedAt IS NULL")
  Optional<AppUser> findActiveById(@Param("id") String id);
  
  @Query("SELECT u FROM AppUser u WHERE u.email = :email AND u.archivedAt IS NULL")
  Optional<AppUser> findActiveByEmail(@Param("email") String email);
}
