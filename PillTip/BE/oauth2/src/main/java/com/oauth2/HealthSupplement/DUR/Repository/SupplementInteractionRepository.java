package com.oauth2.HealthSupplement.DUR.Repository;

import com.oauth2.HealthSupplement.DUR.Entity.HealthSupplementInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplementInteractionRepository extends JpaRepository<HealthSupplementInteraction, Long> {
    List<HealthSupplementInteraction> findBySupplementIdAndDrugId(Long supplementId, Long drugId);
}
