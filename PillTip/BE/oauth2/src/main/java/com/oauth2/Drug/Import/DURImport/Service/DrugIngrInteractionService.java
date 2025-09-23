package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugIngrInteractionService {

    private final SubjectInteractionRepository subjectInteractionRepository;
    private final DrugIngredientRepository drugIngredientRepository;
    private final IngredientRepository ingredientRepository;

    @Value("${ingredient.interaction}")
    private String interaction;

    public void loadIng() throws IOException {
        parseIngredientInteraction(interaction);
    }


    public void parseIngredientInteraction(String filePath) throws IOException {

        try (
                Reader reader = new FileReader(filePath);
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .withAllowMissingColumnNames()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {
                String ingredient1 = record.get(1).trim();       // 첫 번째 컬럼
                String ingredient2 = record.get(2).trim();    // "1세 미만" 등
                String reason = record.get(3).trim();              // 위험성
                String note = record.get(5).trim();

                List<Long> drugId1 = drugIngredientRepository.findDrugIdsByIngredientName(ingredient1);
                List<Long> drugId2 = drugIngredientRepository.findDrugIdsByIngredientName(ingredient2);

                List<Long> ingrId1 = ingredientRepository.findByIngrName(ingredient1);
                List<Long> ingrId2 = ingredientRepository.findByIngrName(ingredient2);

                if(ingrId1.isEmpty() || ingrId2.isEmpty()) continue;

                saveBiInteraction(ingrId1, ingrId2, DurType.DRUGINGR, DurType.DRUGINGR, reason, note);
                saveBiInteraction(drugId1, ingrId2, DurType.DRUG, DurType.DRUGINGR, reason, note);
                saveBiInteraction(ingrId1, drugId2, DurType.DRUGINGR, DurType.DRUG, reason, note);
                saveBiInteraction(drugId1,drugId2,DurType.DRUG,DurType.DRUG, reason, note);
            }
        }
    }

    private void saveBiInteraction(List<Long> idList1, List<Long> idList2, DurType durType1, DurType durType2, String reason, String note) {
        for(Long id1 : idList1){
            for(Long id2: idList2){
                if(!subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(id1,durType1, id2,durType2).isEmpty()) continue;
                saveIngredientInteraction(id1, id2, durType1, durType2, reason, note);
            }
        }
    }

    private void saveIngredientInteraction(Long id1, Long id2, DurType durType1, DurType durType2, String reason, String note) {
        SubjectInteraction subjectInteraction = new SubjectInteraction();
        subjectInteraction.setSubjectId1(id1);
        subjectInteraction.setSubjectId2(id2);
        subjectInteraction.setDurtype1(durType1);
        subjectInteraction.setDurtype2(durType2);
        subjectInteraction.setReason(reason);
        subjectInteraction.setNote(note);

        save(subjectInteraction);
    }

    public SubjectInteraction save(SubjectInteraction subjectInteraction) {
        return subjectInteractionRepository.save(subjectInteraction);
    }
    public void delete(Long id) {
        subjectInteractionRepository.deleteById(id);
    }
    public SubjectInteraction findById(Long id) {
        return subjectInteractionRepository.findById(id).orElse(null);
    }
}
