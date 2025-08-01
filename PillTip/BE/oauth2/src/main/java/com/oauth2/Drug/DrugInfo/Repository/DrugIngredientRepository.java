package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugIngredient;
import com.oauth2.Drug.DrugInfo.Domain.DrugIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrugIngredientRepository extends JpaRepository<DrugIngredient, DrugIngredientId> {
    List<DrugIngredient> findById_IngredientId(Long ingredientId);

    List<DrugIngredient> findById_DrugId(Long id_drugId);
    Optional<DrugIngredient> findById(DrugIngredientId id);

    @Query("""
    SELECT DISTINCT di.id.drugId
    FROM DrugIngredient di
    JOIN Ingredient i ON di.id.ingredientId = i.id
    WHERE LOWER(i.nameEn) LIKE LOWER(CONCAT('%', :name, '%'))
       OR LOWER(:name) LIKE LOWER(CONCAT('%', i.nameEn, '%'))
    """)
    List<Long> findDrugIdsByIngredientName(@Param("name") String name);


} 