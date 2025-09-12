package com.oauth2.Drug.DrugInfo.Service;

import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugIngredient;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DrugService {
    private final DrugRepository drugRepository;
    private final IngredientRepository ingredientRepository;
    private final DrugIngredientRepository drugIngredientRepository;

    public DrugService(DrugRepository drugRepository, IngredientRepository ingredientRepository, DrugIngredientRepository drugIngredientRepository) {
        this.drugRepository = drugRepository;
        this.ingredientRepository = ingredientRepository;
        this.drugIngredientRepository = drugIngredientRepository;
    }



    public List<Drug> findAll() {
        return drugRepository.findAll();
    }
    public Drug save(Drug drug) {
        return drugRepository.save(drug);
    }
    public void delete(Long id) {
        drugRepository.deleteById(id);
    }
    public Drug findById(Long id) {
        return drugRepository.findById(id).orElse(null);
    }
    public Optional<Drug> findByName(String name) {
        return drugRepository.findByName(name);
    }
    public List<Drug> getDrugsByIngredientName(String ingredientName) {
        Ingredient ingredient = ingredientRepository.findByNameKr(ingredientName)
            .orElseThrow(() -> new RuntimeException("해당 성분이 존재하지 않습니다."));
        List<DrugIngredient> drugIngredients = drugIngredientRepository.findById_IngredientId(ingredient.getId());
        List<Long> drugIds = drugIngredients.stream()
            .map(di -> di.getId().getDrugId())
            .toList();
        if (drugIds.isEmpty()) return List.of();
        return drugRepository.findByIdIn(drugIds);
    }
} 