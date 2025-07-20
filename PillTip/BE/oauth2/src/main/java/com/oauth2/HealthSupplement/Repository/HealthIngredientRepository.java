package com.oauth2.HealthSupplement.Repository;

import com.oauth2.HealthSupplement.Entity.HealthIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthIngredientRepository extends JpaRepository<HealthIngredient, Integer> {

    Optional<HealthIngredient> findByName(String ingredientName);

    Optional<HealthIngredient> findByNameAndUnit(String name, String normalizedUnit);
}
