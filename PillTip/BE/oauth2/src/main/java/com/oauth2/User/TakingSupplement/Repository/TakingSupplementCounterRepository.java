package com.oauth2.User.TakingSupplement.Repository;

import com.oauth2.User.TakingPill.Entity.TakingPillCounter;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplementCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TakingSupplementCounterRepository extends JpaRepository<TakingSupplementCounter, Long> {
    Optional<TakingSupplementCounter> findBySupplementId(Long supplementId);
}
