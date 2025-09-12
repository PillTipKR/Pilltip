package com.oauth2.HealthSupplement.SupplementInfo.Repository;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthIngredientRepository extends JpaRepository<HealthIngredient, Integer> {

    Optional<HealthIngredient> findByName(String ingredientName);

    List<HealthIngredient> findByNameContaining(String name);
}
