package com.oauth2.HealthSupplement.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementMat;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementMatRepository;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SupplementInteractionService {

    private final HealthSupplementRepository healthSupplementRepository;
    private final DrugIngredientRepository drugIngredientRepository;
    private final SubjectInteractionRepository subjectInteractionRepository;
    private final HealthSupplementMatRepository supplementMatRepository;
    private final IngredientRepository ingredientRepository;

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
                String ingredient = record.get(2).trim();
                String reason = record.get(3).trim();              // 위험성
                String note = record.get(5).trim();

                List<Long> supplementIds = healthSupplementRepository.findHealthSupplementsByRawMaterial(rawMtrl);
                List<Long> supplementIds2 = healthSupplementRepository.findHealthSupplementsByRawMaterial(ingredient);
                List<Long> drugIds = drugIngredientRepository.findDrugIdsByIngredientName(ingredient);
                List<Long> drugIng = ingredientRepository.findByIngrName(ingredient)
                        .stream()
                        .map(Ingredient::getId)   // 엔티티 → ID
                        .filter(Objects::nonNull)    // 혹시 모를 null 제거
                        .toList();
                List<Long> supplementMat = supplementMatRepository.findByMaterialName(rawMtrl)
                        .stream()
                        .map(HealthSupplementMat::getId)   // 엔티티 → ID
                        .filter(Objects::nonNull)    // 혹시 모를 null 제거
                        .toList();
                List<Long> supplementSecond = supplementMatRepository.findByMaterialName(ingredient)
                        .stream()
                        .map(HealthSupplementMat::getId)   // 엔티티 → ID
                        .filter(Objects::nonNull)    // 혹시 모를 null 제거
                        .toList();

                //건기식성분-건기식성분
                saveInteractionRaw(reason, note, supplementMat, supplementSecond, DurType.SUPINGR, DurType.SUPINGR);
                //건기식성분-약성분
                saveInteractionRaw(reason,note,supplementMat,drugIng, DurType.SUPINGR,DurType.DRUGINGR);
                //건기식성분-약
                saveInteractionRaw(reason,note,supplementMat,drugIds, DurType.SUPINGR,DurType.DRUG);

                //건기식-약 성분
                saveInteractionRaw(reason,note, supplementIds,drugIng, DurType.SUPPLEMENT,DurType.DRUGINGR);
                //건기식-건기식성분
                saveInteractionRaw(reason,note,supplementIds,supplementSecond, DurType.SUPPLEMENT,DurType.SUPPLEMENT);
                //건기식-약
                saveInteractionRaw(reason, note, supplementIds, drugIds, DurType.SUPPLEMENT, DurType.DRUG);
                //건기식-건기식
                saveInteractionRaw(reason,note,supplementIds,supplementIds2, DurType.SUPPLEMENT,DurType.SUPPLEMENT);
            }
        }
    }

    private void saveInteractionRaw(String reason, String note, List<Long> arr1, List<Long> arr2, DurType durType1, DurType durType2) {
        if(!arr1.isEmpty() && !arr2.isEmpty()) {
            for(Long supId : arr1){
                for(Long secId : arr2){
                    if(subjectInteractionRepository.existsSymmetric(supId, durType1, secId, durType2)) continue;

                    saveInteraction(supId,secId,reason,note,durType1,durType2);
                }
            }
        }
    }


    private void saveInteraction(Long supId, Long drugId, String reason, String note, DurType durType1, DurType durType2) {
        SubjectInteraction interaction = new SubjectInteraction();
        interaction.setSubjectId1(supId);
        interaction.setSubjectId2(drugId);
        interaction.setDurtype1(durType1);
        interaction.setDurtype2(durType2);
        interaction.setReason(reason);
        interaction.setNote(note);
        subjectInteractionRepository.save(interaction);
    }

}
