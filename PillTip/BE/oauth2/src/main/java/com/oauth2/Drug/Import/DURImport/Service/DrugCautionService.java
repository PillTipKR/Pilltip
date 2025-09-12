package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DrugCaution;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DUR.Repository.DrugCautionRepository;
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
public class DrugCautionService {

    @Value("${ageFile}")
    private String ageFile;

    @Value("${overdoseFile}")
    private String overdoseFile;

    @Value("${periodFile}")
    private String periodFile;

    @Value("${elderFile}")
    private String elderFile;

    @Value("${pregnancyFile}")
    private String pregnancyFile;

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


    private final DrugCautionRepository drugCautionRepository;
    private final DrugRepository drugRepository;
    private final DrugIngredientRepository drugIngredientRepository;

    private DrugCaution.ConditionType convertConditionType(String type) {
        return switch (type.trim()) {
            case "투여기간주의" -> DrugCaution.ConditionType.PERIOD;
            case "임부금기"     -> DrugCaution.ConditionType.PREGNANCY;
            case "연령금기"     -> DrugCaution.ConditionType.AGE;
            case "노인주의"     -> DrugCaution.ConditionType.ELDER;
            case "수유부주의"   -> DrugCaution.ConditionType.LACTATION;
            case "용량주의" -> DrugCaution.ConditionType.OVERDOSE;
            default -> throw new IllegalArgumentException("지원되지 않는 유형: " + type);
        };
    }

    //노인(65세 이상)
    public void parseElderCautions(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기

        String productName = null;
        String conditionTypeStr = null;
        String conditionValue = "";
        String note = "";

        for (String line : lines) {
            line = line.trim(); // 공백 제거

            if (line.isEmpty()) {
                // 빈 줄이 나오면 하나의 블록이 끝났다는 뜻 → 저장
                if (productName != null && conditionTypeStr != null) {
                    saveCaution(productName, convertConditionType(conditionTypeStr), conditionValue, note);
                }

                // 초기화
                productName = conditionTypeStr = conditionValue = note = null;
                continue;
            }

            if (line.startsWith("제품명: ")) {
                productName = line.replace("제품명: ", "").trim();
            } else if (line.startsWith("유형명: ")) {
                conditionTypeStr = line.replace("유형명: ", "").trim();
            } else if (line.startsWith("금기내용:")) {
                conditionValue = line.replace("금기내용:", "").trim();
            } else if (line.startsWith("비고:")) {
                note = line.replace("비고:", "").trim();
            }
        }

        // 마지막 항목 저장
        if (productName != null && conditionTypeStr != null) {
            saveCaution(productName, convertConditionType(conditionTypeStr), conditionValue, note);
        }
    }


    public void parsePregnancyCautions(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기

        String productName = null;
        String conditionValue = "";
        String note = "";

        for (String line : lines) {
            line = line.trim(); // 공백 제거

            // 제품명 추출
            if (line.startsWith("제품명: ")) {
                productName = line.replace("제품명: ", "").trim();
            }
            // 임부금기내용
            else if (line.contains("임부금기내용:")) {
                conditionValue = line.substring(line.indexOf("임부금기내용:") + "임부금기내용:".length()).trim();
            }
            // 비고
            else if (line.startsWith("비고:")) {
                note = line.replace("비고:", "").trim();
            }
            // 구분선(또는 다음 데이터 시작)
            else if (line.startsWith("==================")) {
                if (productName != null && !productName.isBlank()) {
                    saveCaution(productName, DrugCaution.ConditionType.PREGNANCY, conditionValue, note);
                }

                // 초기화
                productName = conditionValue = note = null;
            }
        }

        // 마지막 데이터 처리
        if (productName != null && !productName.isBlank()) {
            saveCaution(productName, DrugCaution.ConditionType.PREGNANCY, conditionValue, note);
        }

    }


    public void parseAgeCautions(String filePath) throws IOException {
        try (
                Reader reader = new FileReader(filePath);
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withIgnoreEmptyLines()
                        .withAllowMissingColumnNames()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {
                String productName = record.get(0).trim();       // 첫 번째 컬럼
                String conditionValue = record.get(7).trim();    // "1세 미만" 등
                String note = record.get(8).trim();              // 위험성

                saveCaution(productName, DrugCaution.ConditionType.AGE, conditionValue, note);
            }
        }
    }


    public void parseOverdoseCautions(String filePath) throws IOException {

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
                String productName = record.get(1).trim();       // 첫 번째 컬럼
                String conditionValue = record.get(4).trim();    // "1세 미만" 등
                String note = record.get(5).trim();              // 위험성

                saveCaution(productName, DrugCaution.ConditionType.OVERDOSE, conditionValue, note);
            }
        }
    }

    public void parsePeriodCautions(String filePath) throws IOException {
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
                String productName = record.get(0).trim();       // 첫 번째 컬럼
                String conditionValue = record.get(4).trim();    // "1세 미만" 등
                String note = "";              // 위험성

                saveCaution(productName, DrugCaution.ConditionType.PERIOD, conditionValue, note);
            }
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

    private void parseIngrCautions(String filePath, DrugCaution.ConditionType conditionType) throws IOException {
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

                List<Long> drugIds = drugIngredientRepository.findDrugIdsByIngredientName(ingrName);

                if(drugIds.isEmpty()) continue;

                for(Long id : drugIds){
                    if(!drugCautionRepository.findByDrugIdAndConditionType(id, conditionType).isEmpty()) continue;
                    saveIngredientCaution(id, conditionType,conditionValue, note);
                }
            }
        }
    }

    public void parseIngrAll() throws IOException {
        parseIngrCautions(ingredientAge, DrugCaution.ConditionType.AGE);
        parseIngrCautions(ingredientElder, DrugCaution.ConditionType.ELDER);
        parseIngrCautions(ingredientLactation, DrugCaution.ConditionType.LACTATION);
        parseIngrCautions(ingredientPeriod, DrugCaution.ConditionType.PERIOD);
        parseIngrCautions(ingredientPregnant, DrugCaution.ConditionType.PREGNANCY);
    }

    private void saveIngredientCaution(Long id, DrugCaution.ConditionType conditionType, String conditionValue, String note) {
        DrugCaution caution = new DrugCaution();
        caution.setDrugId(id);
        caution.setConditionType(conditionType);
        caution.setConditionValue(conditionValue);
        caution.setNote(note);
        drugCautionRepository.save(caution);
    }

    private void saveCaution(String productName, DrugCaution.ConditionType conditionTypeStr, String conditionValue, String note) {
        productName = removeLeadingParentheses(productName);
        List<Drug> drugs = drugRepository.findByNameContaining(productName);
        if (!drugs.isEmpty()) {
            Drug drug = drugs.get(0);
            if(!drugCautionRepository.findByDrugIdAndConditionType(drug.getId(), conditionTypeStr).isEmpty()) return;
            DrugCaution caution = new DrugCaution();
            caution.setDrugId(drug.getId());
            caution.setConditionType(conditionTypeStr);
            caution.setConditionValue(conditionValue);
            caution.setNote(note);

            drugCautionRepository.save(caution);
        } else {
            System.out.println(" 약품명 [" + productName + "] 을(를) 찾을 수 없습니다.");
        }
    }

    public void parseAllAndSave() throws IOException {
        parseElderCautions(elderFile);
        parsePregnancyCautions(pregnancyFile);
        parseAgeCautions(ageFile);
        parseOverdoseCautions(overdoseFile);
        parsePeriodCautions(periodFile);
    }


    public DrugCaution save(DrugCaution drugCaution) {
        return drugCautionRepository.save(drugCaution);
    }
    public void delete(Long id) {
        drugCautionRepository.deleteById(id);
    }
    public DrugCaution findById(Long id) {
        return drugCautionRepository.findById(id).orElse(null);
    }
}
