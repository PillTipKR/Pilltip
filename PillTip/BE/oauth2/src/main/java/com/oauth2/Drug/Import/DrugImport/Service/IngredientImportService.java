package com.oauth2.Drug.Import.DrugImport.Service;

import com.oauth2.Drug.DrugInfo.Domain.*;
import com.oauth2.Drug.DrugInfo.Repository.*;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientImportService {

    private final IngredientRepository ingredientRepository;
    private final Logger logger = LoggerFactory.getLogger(IngredientImportService.class);

    public void importIngredientsFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            reader.readNext(); // skip header
            List<Ingredient> ingredientList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameKr = line[0].trim();
                String nameEn = line[4].trim().toLowerCase();

                Optional<Ingredient> ing = ingredientRepository.findByNameKr(nameKr);

                if (ing.isPresent()) {
                    ing.get().setNameEn(nameEn);
                    ingredientList.add(ing.get());
                }
            }

            ingredientRepository.saveAll(ingredientList);
        } catch (Exception e) {
            logger.error("Error occurred in import ingredient: {}", e.getMessage());
        }
    }

}
