package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.TakingPillCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TakingPillCounterRepository extends JpaRepository<TakingPillCounter, Long> {
    Optional<TakingPillCounter> findByDrugId(Long drugId);
}
