package io.ghassen.pockito.subscription.infrastructure.persistence.repository;

import io.ghassen.pockito.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findByUserUsernameOrderByNameAsc(String username);

    List<Subscription> findByUserUsernameAndEnabledTrueOrderByNameAsc(String username);

    Optional<Subscription> findByUserUsernameAndId(String username, String subscriptionId);

    List<Subscription> findByNextDueDateLessThanEqual(LocalDate date);

    List<Subscription> findByEnabledTrueAndNextDueDateLessThanEqual(LocalDate date);

    long countByUserUsername(String username);

    long countByUserUsernameAndEnabledTrue(String username);
}


