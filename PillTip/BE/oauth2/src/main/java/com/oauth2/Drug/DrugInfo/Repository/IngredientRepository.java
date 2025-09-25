package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByNameKr(String nameKr);

    List<Ingredient> findByNameEn(String nameEng);

    @Query("""
    SELECT i FROM Ingredient i WHERE LOWER(i.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(:keyword) LIKE LOWER(CONCAT('%', i.nameEn, '%'))
    """)
    List<Long> findByIngrName(String keyword);

    Optional<Ingredient> findById(Long id);
} 