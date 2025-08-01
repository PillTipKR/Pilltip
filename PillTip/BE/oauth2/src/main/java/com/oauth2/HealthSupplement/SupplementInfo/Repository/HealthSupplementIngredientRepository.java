package com.oauth2.HealthSupplement.SupplementInfo.Repository;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthSupplementIngredientRepository extends JpaRepository<HealthSupplementIngredient, Long> {
    List<HealthSupplementIngredient> findBySupplementId(Long id);
}
