package com.oauth2.Drug.Import.DrugImport.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.oauth2.Drug.DrugInfo.Domain.*;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugTagRepository;
import com.oauth2.Drug.DrugInfo.Service.*;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DrugImportService {
    private final DrugService drugService;
    private final IngredientService ingredientService;
    private final DrugIngredientService drugIngredientService;
    private final DrugEffectService drugEffectService;
    private final DrugStorageConditionService drugStorageConditionService;
    private final DrugRepository drugRepository;
    private final DrugTagRepository drugTagRepository;
    private static final Logger logger = LoggerFactory.getLogger(DrugImportService.class);

    public void importFromFile(String filePath) throws IOException {
        String allText = Files.readString(Path.of(filePath));
        String[] blocks = allText.split("(?=ITEM_SEQ :)"); // ITEM_SEQ : 로 시작하는 줄마다 분리
        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                importFromBlock(block.trim());
            }
        }
    }

    public void importFromBlock(String block) {
        Map<String, String> data = parseKeyValue(block);  // 라벨:값 구조 파싱
        String name = data.get("ITEM_NAME");
        String code = data.get("ITEM_SEQ");
        String manufacturer = data.get("ENTP_NAME");
        String packaging = data.get("PACK_UNIT");
        String form = data.get("CHART");
        String atcCode = data.get("ATC_CODE");
        String approvalDate = data.get("ITEM_PERMIT_DATE");
        String tag = data.get("ETC_OTC_CODE");
        String ingredientLine = data.get("MATERIAL_NAME");
        String storageMethod = data.get("STORAGE_METHOD");
        String validFrom = data.get("VALID_TERM");

        String effect = extractBetween(block, "EE_DOC_DATA :", "UD_DOC_DATA :");
        String usage = extractBetween(block, "UD_DOC_DATA :", "NB_DOC_DATA :");
        String caution = extractBetween(block, "NB_DOC_DATA :", "MAIN_ITEM_INGR :");
        // name이 없으면 Drug 저장 건너뜀
        if (name == null || name.isEmpty()) {
            System.out.println("제품명 누락 블록 건너뜀: " + block);
            return;
        }

        // 이미 존재하는 약이면 스킵
        if (drugRepository.findByName(name).isPresent()) return;

        // Drug 저장
        Optional<Drug> drugOpt = drugService.findByName(name);
        Drug drug;
        if (drugOpt.isPresent()) {
            drug = drugOpt.get();
        } else {
            drug = new Drug();
            drug.setName(name);
            drug.setCode(code);
            drug.setManufacturer(manufacturer);
            drug.setPackaging(packaging);
            drug.setForm(form);
            drug.setAtcCode(atcCode);
            drug.setValidTerm(validFrom);

            if (approvalDate != null && approvalDate.matches("\\d{8}")) {
                try {
                    LocalDate localDate = LocalDate.parse(approvalDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    drug.setApprovalDate(Date.valueOf(localDate));
                } catch (Exception e) {
                    System.out.println("허가일자 파싱 오류: " + approvalDate);
                }
            }

            if (tag != null) {
                drug.setTag(tag.equals("일반의약품") ? Drug.Tag.COMMON : Drug.Tag.EXPERT);
            }

            drug = drugService.save(drug);
        }

        // 성분 파싱 및 저장
        if (ingredientLine != null && !ingredientLine.isEmpty()) {
            String[] ingredients = ingredientLine.split(";");
            for (String ing : ingredients) {
                String[] parts = ing.split("\\|");
                String nameKr = null, amount = "", unit = null;

                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("성분명 :")) nameKr = part.replace("성분명 :", "").trim();
                    else if (part.startsWith("분량 :")) amount = part.replace("분량 :", "").trim();
                    else if (part.startsWith("단위 :")) unit = part.replace("단위 :", "").trim();
                }

                if (nameKr == null) continue;

                Optional<Ingredient> ingOpt = ingredientService.findByNameKr(nameKr);
                Ingredient ingredient;
                if (ingOpt.isPresent()) {
                    ingredient = ingOpt.get();
                } else {
                    ingredient = new Ingredient();
                    ingredient.setNameKr(nameKr);
                    ingredient.setNameEn("v");
                    ingredient = ingredientService.save(ingredient);
                }

                DrugIngredient di = new DrugIngredient();
                DrugIngredientId diId = new DrugIngredientId();
                parseIngredientAmount(di, amount);
                diId.setDrugId(drug.getId());
                diId.setIngredientId(ingredient.getId());
                di.setId(diId);
                di.setUnit(unit);
                drugIngredientService.save(di);
            }
        }

        // 효과/용법/주의사항 저장
        String filteredEffect = filterContent(effect);
        String filteredUsage = filterContent(usage);
        String filteredCaution = filterContent(caution);

        //effect 저장
        saveAllEffect(drug, filteredUsage,filteredEffect,filteredCaution);

        /*
        - 온도조건: 실온에서 보관한다.
        - 용기: 밀봉용기
        - 습도: 정보 없음
        - 차광: 정보 없음
        */
        DrugStorageCondition temperature = new DrugStorageCondition();
        DrugStorageCondition pack = new DrugStorageCondition();
        DrugStorageCondition humid = new DrugStorageCondition();
        DrugStorageCondition light = new DrugStorageCondition();


        temperature.setCategory(DrugStorageCondition.Category.TEMPERATURE);
        pack.setCategory(DrugStorageCondition.Category.CONTAINER);
        light.setCategory(DrugStorageCondition.Category.LIGHT);
        humid.setCategory(DrugStorageCondition.Category.HUMID);
        pack.setDrug(drug);
        light.setDrug(drug);
        temperature.setDrug(drug);
        humid.setDrug(drug);

        List<String> storage = parseStorageMethod(storageMethod);
        for(String s : storage) {
            if(s.contains("용기")) {
                pack.setValue(s);
                pack.setActive(true);
            }
            if(s.contains("광")) {
                light.setValue(s);
                light.setActive(true);
            }

            Matcher m = Pattern.compile("(\\d+)+℃").matcher(s);
            if(s.contains("실온") || m.find() || s.contains("고온") || s.contains("저온")) {
                temperature.setValue(s);
                temperature.setActive(true);
            }

            if(s.contains("건냉") || s.contains("건조") || s.contains("습기")){
                humid.setValue(s);
                humid.setActive(true);

            }

        }

        drugStorageConditionService.save(temperature);
        drugStorageConditionService.save(pack);
        drugStorageConditionService.save(humid);
        drugStorageConditionService.save(light);
    }


    private void saveAllEffect(Drug drug, String usage, String effect, String caution) {
        saveEffect(drug, DrugEffect.Type.USAGE, usage);
        saveEffect(drug, DrugEffect.Type.EFFECT, effect);
        saveEffect(drug, DrugEffect.Type.CAUTION, caution);
    }

    private void saveEffect(Drug drug, DrugEffect.Type type, String content){
        if (!content.isEmpty()) {
            DrugEffect de = new DrugEffect();
            de.setDrug(drug);
            de.setContent(content);
            de.setType(type);
            drugEffectService.save(de);
        }
    }


    private String filterContent(String content) {
        // 개행 정리 + 앞뒤 공백 제거
        return content
                .replaceAll("(\\n\\s*){2,}", "\n")  // 2줄 이상 연속 개행 -> 1줄 개행
                .replaceAll(" +", " ")              // 중복 공백 제거
                .replaceAll("={60,}","")
                .trim();
    }

    private void parseIngredientAmount(DrugIngredient di,String amount) {
        // 1. 숫자와 단위를 분리하고 불필요한 공백이나 특수문자 제거
        amount = amount.trim();

        // 연속된 숫자 처리 (예: 1,0001,000 -> 1,000으로 처리)
        if (amount.length() > 1 &&
                amount.substring(0,amount.length()/2)
                        .equals(amount.substring(amount.length()/2))) {
            amount = amount.substring(0, amount.length()/2); // 첫 번째 숫자까지만 추출
        }
        amount = removeDuplicateAmount(amount);
        String numericAmountBackup = amount;
        // 숫자만 남기기 (콤마 제거, "이상" 같은 불필요한 단어 처리)
        String numericAmount = amount.replaceAll("[^0-9.:~]", "").replaceAll("\\.{2,}","");


        // '이상', '역가'와 같은 특수 단위 처리
        if (numericAmount.contains("이상") || numericAmount.contains("역가")) {
            // "이상"이나 "역가"와 같은 단위는 무시하거나 별도의 처리
            numericAmount = numericAmount.replaceAll("이상|역가", "").trim();
        }

        try {
            // 숫자형 데이터로 변환
            if (!numericAmount.isEmpty()) {
                di.setAmount(Float.parseFloat(numericAmount));
                di.setAmountBackup(numericAmountBackup);
            }
        } catch (NumberFormatException e) {
            // 숫자 형식 오류 처리
            //System.out.println("성분 분량 파싱 오류: " + numericAmount);
            di.setAmountBackup(numericAmountBackup);
        }
    }

    // 숫자와 단위가 반복되는 경우를 처리하는 메서드
    private String removeDuplicateAmount(String amount) {
        // 숫자 뒤에 단위가 두 번 이상 반복되는 경우, 첫 번째 값만 유지하고 두 번째 값을 제거
        // 예: 7.953mg7.953 -> 7.953mg
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)([a-zA-Z]+)(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(amount);

        // 만약 단위가 뒤에 반복되면 이를 제거
        if (matcher.find()) {
            amount = matcher.replaceAll("$1");  // 첫 번째 숫자와 단위만 남김
        }

        return amount;
    }


    private Map<String, String> parseKeyValue(String content) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] lines = content.split("\\r?\\n");
        String currentKey = null;
        StringBuilder valueBuffer = new StringBuilder();

        for (String line : lines) {
            if (line.contains(" : ")) {
                if (currentKey != null) {
                    result.put(currentKey.trim(), valueBuffer.toString().trim());
                }
                String[] parts = line.split(":", 2);
                currentKey = parts[0].trim();
                valueBuffer = new StringBuilder(parts[1].trim());
            } else if (currentKey != null) {
                valueBuffer.append(" ").append(line.trim());
            }
        }

        if (currentKey != null) {
            result.put(currentKey.trim(), valueBuffer.toString().trim());
        }
        return result;
    }

    private List<String> parseStorageMethod(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    private String extractBetween(String text, String startLabel, String endLabel) {
        int start = text.indexOf(startLabel);
        int end = text.indexOf(endLabel);
        if (start == -1 || end == -1 || end <= start) return "";
        return text.substring(start + startLabel.length(), end).trim();
    }

    public void importImageFromCsv(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            reader.readNext(); // skip header
            List<Drug> drugList = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                String nameKr = line[0].trim();
                String imageUrl = line[1].trim();

                Drug drug = matchDrugByNameVariants(nameKr);
                if(drug != null) {
                    drug.setImage(imageUrl);
                    drugList.add(drug);
                    //System.out.println(nameKr + " " + imageUrl);
                }else
                    System.out.println(nameKr + " 을 찾을 수 없습니다.");
            }
            drugRepository.saveAll(drugList);
        } catch (Exception e) {
            logger.error("Error occurred in import image: {}", e.getMessage());
        }
    }

    public void importEffectTag(String filePath) throws IOException {
        String allText = Files.readString(Path.of(filePath));
        String[] blocks = allText.split("\n");
        for(String block : blocks) {
            String[] contents = block.split("\t");
            Long id = Long.parseLong(contents[0].trim());
            String type = contents[1].trim();
            type = type.isEmpty()? "":type;
            DrugTag d = new DrugTag();
            d.setDrugId(id);
            d.setTag(type);
            drugTagRepository.save(d);
        }
    }


    private String normalizeDrugName(String name) {
        name = Normalizer.normalize(name, Normalizer.Form.NFKC);
        name = name.replaceAll("[\\s\\u200B\\uFEFF]+", "");
        name = removeSquareBrackets(name);
        return name.trim();
    }

    private String removeSquareBrackets(String name) {
        // 예: "약이름 [수출명: ~~~]" → "약이름"
        return name.replaceAll("\\[.*?]", "").trim();
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

    protected Drug matchDrugByNameVariants(String name) {
        // 1단계: 원래 이름 기준 매칭
        for (Drug drug : drugService.findAll()) {
            if (drug.getName().equals(name)) return drug;
        }

        // 2단계: 괄호 제거 후 매칭
        String noParen = removeLeadingParentheses(name);
        for (Drug drug : drugService.findAll()) {
            if (removeLeadingParentheses(drug.getName()).equals(noParen)) return drug;
        }

        // 3단계: 정규화 후 공백 제거 매칭
        String normalized = normalizeDrugName(noParen);
        for (Drug drug : drugService.findAll()) {
            String targetNorm = normalizeDrugName(drug.getName());
            if (targetNorm.equals(normalized)) return drug;
        }

        return null;
    }

}
