package io.ghassen.pockito.repo;

import io.ghassen.pockito.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
  // Basic CRUD operations are inherited from JpaRepository
}
