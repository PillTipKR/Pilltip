package com.oauth2.HealthSupplement.Repository;

import com.oauth2.HealthSupplement.Entity.HealthSupplementIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthSupplementIngredientRepository extends JpaRepository<HealthSupplementIngredient, Long> {
}
