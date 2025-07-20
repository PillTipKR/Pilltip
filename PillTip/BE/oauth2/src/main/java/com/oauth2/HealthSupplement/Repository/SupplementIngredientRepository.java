package com.oauth2.HealthSupplement.Repository;

import com.oauth2.HealthSupplement.Entity.SupplementIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplementIngredientRepository extends JpaRepository<SupplementIngredient, Long> {
}
