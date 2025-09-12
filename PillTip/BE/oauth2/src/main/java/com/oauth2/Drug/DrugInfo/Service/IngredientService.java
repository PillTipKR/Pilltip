package com.oauth2.Drug.DrugInfo.Service;

import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }
    public Ingredient save(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }
    public void delete(Long id) {
        ingredientRepository.deleteById(id);
    }
    public Ingredient findById(Long id) {
        return ingredientRepository.findById(id).orElse(null);
    }
    public Optional<Ingredient> findByNameKr(String nameKr) {
        return ingredientRepository.findByNameKr(nameKr);
    }
} 