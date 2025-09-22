package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugInteractionService {

    private final SubjectInteractionRepository subjectInteractionRepository;
    private final DrugIngredientRepository drugIngredientRepository;
    private final DrugRepository drugRepository;

    @Value("${interactionFile1}")
    private String interactionFile1;

    @Value("${interactionFile2}")
    private String interactionFile2;

    @Value("${ingredient.interaction}")
    private String interaction;


    public void loadAll() throws IOException {
        parseInteractionCautions(interactionFile1);
        parseInteractionCautions(interactionFile2);

    }

    public void loadIng() throws IOException {
        parseIngredientInteraction(interaction);
    }

    private void parseInteractionCautions(String path) throws IOException {
        String content = Files.readString(Paths.get(path));
        String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기

        String productName1 = null;
        String productName2 = null;
        String reason = "";
        String note = "";

        for (String line : lines) {
            line = line.trim(); // 공백 제거

            // 제품명 추출
            if (line.startsWith("제품명: ")) {
                productName1 = line.replace("제품명: ", "").trim();
            }
            else if (line.startsWith("병용 제품명: ")) {
                productName2 = line.replace("병용 제품명: ", "").trim();
            }
            // 임부금기내용
            else if (line.contains("금기내용:")) {
                reason = line.substring(line.indexOf("금기내용:") + "금기내용:".length()).trim();
            }
            // 비고
            else if (line.startsWith("비고:")) {
                note = line.replace("비고:", "").trim();
            }
            // 구분선(또는 다음 데이터 시작)
            else if (line.startsWith("==================")) {
                if ((productName1 != null && !productName1.isBlank())
                        && (productName2 != null && !productName2.isBlank())) {
                    saveDrugInteraction(productName1, productName2, reason, note);
                    saveDrugInteraction(productName2, productName1, reason, note);
                }

                // 초기화
                productName1 = productName2 = reason = note = null;
            }
        }

        // 마지막 데이터 처리
        if ((productName1 != null && !productName1.isBlank())
                && (productName2 != null && !productName2.isBlank())) {
            saveDrugInteraction(productName1, productName2, reason, note);
            saveDrugInteraction(productName2, productName1, reason, note);
        }

    }

    private String removeLeadingParentheses(String name) {
        while (name.startsWith("(")) {
            int depth = 0;
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == '(') depth++;
                else if (name.charAt(i) == ')') {
                    depth--;
                    if (depth == 0) {
                        name = name.substring(i + 1).strip();
                        break;
                    }
                }
            }
        }
        return name.split("\\(")[0];
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

                if(drugId1.isEmpty() || drugId2.isEmpty()) continue;

                for(Long id1 : drugId1){
                    for(Long id2: drugId2){
                        if(!subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(id1,DurType.DRUG, id2,DurType.DRUG).isEmpty()) continue;
                        saveIngredientInteraction(id1, id2, reason, note);
                        if(!subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(id2,DurType.DRUG,id1,DurType.DRUG).isEmpty()) continue;
                        saveIngredientInteraction(id2, id1, reason, note);
                    }
                }
            }
        }
    }

    private void saveIngredientInteraction(Long id1, Long id2, String reason, String note) {
        SubjectInteraction subjectInteraction = new SubjectInteraction();
        subjectInteraction.setSubjectId1(id1);
        subjectInteraction.setSubjectId2(id2);
        subjectInteraction.setDurtype1(DurType.DRUG);
        subjectInteraction.setDurtype2(DurType.DRUG);
        subjectInteraction.setReason(reason);
        subjectInteraction.setNote(note);

        save(subjectInteraction);
    }

    private void saveDrugInteraction(String pName1, String pName2, String reason, String note){
        pName1 = removeLeadingParentheses(pName1);
        pName2 = removeLeadingParentheses(pName2);
        List<Drug> idList1 = drugRepository.findByNameContaining(pName1);
        List<Drug> idList2 = drugRepository.findByNameContaining(pName2);

        if(!idList1.isEmpty() && !idList2.isEmpty()) {
            Drug id1 = idList1.get(0);
            Drug id2 = idList2.get(0);
            List<SubjectInteraction> drugInter =
                    subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(id1.getId(), DurType.DRUG,id2.getId(),DurType.DRUG);
            if(drugInter.isEmpty()) {
                SubjectInteraction subjectInteraction = new SubjectInteraction();
                subjectInteraction.setSubjectId1(id1.getId());
                subjectInteraction.setSubjectId2(id2.getId());
                subjectInteraction.setDurtype1(DurType.DRUG);
                subjectInteraction.setDurtype2(DurType.DRUG);
                subjectInteraction.setReason(reason);
                subjectInteraction.setNote(note);

                save(subjectInteraction);
            }

        }else {
            System.out.println("약품명 [" + pName1 + " 혹은 " + pName2 + "]  을(를) 찾을 수 없습니다.");
        }
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
