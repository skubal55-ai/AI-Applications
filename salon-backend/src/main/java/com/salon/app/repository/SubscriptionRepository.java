package com.salon.app.repository;

import com.salon.app.model.Subscription;
import com.salon.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByCustomerAndStatus(User customer, Subscription.SubscriptionStatus status);
    List<Subscription> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
}
