package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.ConditionType;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectCaution;
import com.oauth2.Drug.DUR.Repository.SubjectCautionRepository;
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
public class DrugIngrCautionService {

    @Value("${ingredient.age}")
    private String ingredientAge;

    @Value("${ingredient.elder}")
    private String ingredientElder;

    @Value("${ingredient.period}")
    private String ingredientPeriod;

    @Value("${ingredient.lactation}")
    private String ingredientLactation;

    @Value("${ingredient.pregnant}")
    private String ingredientPregnant;


    private final SubjectCautionRepository subjectCautionRepository;
    private final IngredientRepository ingredientRepository;

    private void parseIngrCautions(String filePath, ConditionType conditionType) throws IOException {
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
                String ingrName = record.get(0).trim();       // 첫 번째 컬럼
                String conditionValue = record.get(2).trim();    // "1세 미만" 등
                String note = record.get(3).trim();              // 위험성

                List<Long> ingrIds = ingredientRepository.findByIngrName(ingrName);

                if(ingrIds.isEmpty()) continue;

                for(Long id : ingrIds){
                    if(!subjectCautionRepository.findBySubjectIdAndConditionType(id, conditionType).isEmpty()) continue;
                    saveIngredientCaution(id, conditionType,conditionValue, note);
                }
            }
        }
    }

    public void parseIngrAll() throws IOException {
        parseIngrCautions(ingredientAge, ConditionType.AGE);
        parseIngrCautions(ingredientElder, ConditionType.ELDER);
        parseIngrCautions(ingredientLactation, ConditionType.LACTATION);
        parseIngrCautions(ingredientPeriod, ConditionType.PERIOD);
        parseIngrCautions(ingredientPregnant, ConditionType.PREGNANCY);
    }

    private void saveIngredientCaution(Long id, ConditionType conditionType, String conditionValue, String note) {
        SubjectCaution caution = new SubjectCaution();
        caution.setSubjectId(id);
        caution.setDurtype(DurType.DRUGINGR);
        caution.setConditionType(conditionType);
        caution.setConditionValue(conditionValue);
        caution.setNote(note);
        subjectCautionRepository.save(caution);
    }

    public SubjectCaution save(SubjectCaution subjectCaution) {
        return subjectCautionRepository.save(subjectCaution);
    }
    public void delete(Long id) {
        subjectCautionRepository.deleteById(id);
    }
    public SubjectCaution findById(Long id) {
        return subjectCautionRepository.findById(id).orElse(null);
    }
}
