package com.oauth2.HealthSupplement.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
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
public class SupplementInteractionService {

    private final HealthSupplementRepository healthSupplementRepository;
    private final DrugIngredientRepository drugIngredientRepository;
    private final SubjectInteractionRepository subjectInteractionRepository;

    @Value("${supplement.interaction}")
    private String interaction;

    public void loadIng() throws IOException {
        parseIngredientInteraction(interaction);
    }

    private void parseIngredientInteraction(String filePath) throws IOException {

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
                String rawMtrl = record.get(1).trim();       // 첫 번째 컬럼
                String ingredient = record.get(2).trim();    // "1세 미만" 등
                String reason = record.get(3).trim();              // 위험성
                String note = record.get(5).trim();

                List<Long> supplementIds = healthSupplementRepository.findHealthSupplementsByRawMaterial(rawMtrl);
                List<Long> drugIds = drugIngredientRepository.findDrugIdsByIngredientName(ingredient);

                if(supplementIds.isEmpty() || drugIds.isEmpty()) continue;

                for(Long supId : supplementIds){
                    for(Long drugId : drugIds){
                        if(!subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(drugId,DurType.DRUG, supId, DurType.SUPPLEMENT).isEmpty()) continue;
                        saveInteraction(supId,drugId,reason,note);
                    }
                }
            }
        }
    }


    private void saveInteraction(Long supId, Long drugId, String reason, String note) {
        SubjectInteraction interaction = new SubjectInteraction();
        interaction.setSubjectId1(drugId);
        interaction.setSubjectId2(supId);
        interaction.setDurtype1(DurType.DRUG);
        interaction.setDurtype2(DurType.SUPPLEMENT);
        interaction.setReason(reason);
        interaction.setNote(note);
        subjectInteractionRepository.save(interaction);
    }

}
