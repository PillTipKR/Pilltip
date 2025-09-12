package com.oauth2.Drug.Import.DrugImport.Service;

import com.oauth2.Drug.DrugInfo.Domain.*;
import com.oauth2.Drug.DrugInfo.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PromptDrugUpdater {

    private final DrugEffectService drugEffectService;
    private final DrugIngredientService drugIngredientService;
    private final DrugStorageConditionService drugStorageConditionService;
    private final IngredientService ingredientService;
    private final DrugService drugService;

    @Transactional
    public void updatePromptDataForDrug(Drug drug, Map<String, String> sectionMap) {
        Drug freshDrug = drugService.findById(drug.getId());

        if (sectionMap.containsKey("1")) updateEffect(freshDrug, sectionMap.get("1"));
        if (sectionMap.containsKey("2")) updateUsage(freshDrug, sectionMap.get("2"));
        if (sectionMap.containsKey("3")) updateIngredients(freshDrug, sectionMap.get("3"));
        if (sectionMap.containsKey("4")) updateStorage(freshDrug, sectionMap.get("4"));
        if (sectionMap.containsKey("5")) updateForm(freshDrug, sectionMap.get("5"));

    }

    private void updateEffect(Drug drug, String content) {
        if (content == null || content.isBlank()) return;
        DrugEffect effect = findOrCreateEffect(drug, DrugEffect.Type.EFFECT);
        effect.setContent(clean(content).trim());
        drugEffectService.save(effect);
    }

    private void updateUsage(Drug drug, String content) {
        if (content == null || content.isBlank()) return;
        DrugEffect usage = findOrCreateEffect(drug, DrugEffect.Type.USAGE);
        usage.setContent(clean(content).replaceAll("복용 정보", "용법/용량").trim());
        drugEffectService.save(usage);
    }

    private String clean(String content) {
        String clean = cleanAllFormatting(content);
        return insertLineBreaks(clean);
    }

    private String insertLineBreaks(String text) {
        // "에요." 또는 "에요.**" 다음에 줄바꿈 추가
        return text.replaceAll("(요\\.\\s+)", "$1\n").strip();
    }

    private String cleanAllFormatting(String input) {
        if (input == null) return "";

        String result = input;

        // 1. 역슬래시 제거
        result = result.replaceAll("\\\\", "");

        // 2. HTML 태그 제거
        result = result.replaceAll("<[^>]*>", "");

        // 3. HTML 특수문자 제거 (기본 escape 문자)
        result = result.replaceAll("&[a-zA-Z]+;", "");

        // 4. 마크다운 제목 라인 제거: ## 1. 제목 or ### 제목 등
        result = result.replaceAll("(?m)^\\s*#{1,6}\\s*\\d*\\.?\\s*.*$", "");


        // 5. 마크다운 특수문자 제거
        String[] markdownSymbols = {
                "\\*\\*",  // **
                "~~",      // ~~
                "`+",      // ` or ```
                "#+",      // #, ## 등
                "^>\\s*",  // 인용문 '>'는 문장 앞에 오는 경우만 제거 (문장 내부 > 는 보존)
                "^\\-\\s*", // 리스트 '-' 문장 앞에서만 제거
                "^\\+\\s*", // 리스트 '+' 문장 앞에서만 제거
                "^\\d+\\.\\s*", // 숫자 리스트 항목: '1. ', '2. ' 등
        };

        for (String symbol : markdownSymbols) {
            result = result.replaceAll(symbol, "");
        }

        return result;
    }

    private void updateIngredients(Drug drug, String block) {
        // 줄별 정보 추출
        String nameLine = extractLine(block, "- 성분명:");
        String amountLine = extractLine(block, "- 분량:");
        String unitLine = extractLine(block, "- 단위:");

        // 쉼표 기준 분리
        String[] names = nameLine.split(",");
        String[] amounts = amountLine.split(",");
        String[] units = unitLine.split(",");

        for (int i = 0; i < names.length; i++) {
            String name = names[i].trim();
            Optional<Ingredient> matchedOpt = matchIngredientByName(name);
            if (matchedOpt.isEmpty()) {
                continue;
            }

            Ingredient ingredient = matchedOpt.get();

            // 인덱스 기반 값 적용 (없으면 기본값)
            String amountStr = (i < amounts.length) ? amounts[i].trim() : "";
            String unit = (units.length == 1) ? units[0].trim() : (i < units.length ? units[i].trim() : "");

            DrugIngredientId id = new DrugIngredientId();
            id.setDrugId(drug.getId());
            id.setIngredientId(ingredient.getId());

            DrugIngredient di = drugIngredientService.findById(id)
                    .orElseGet(() -> {
                        DrugIngredient newDi = new DrugIngredient();
                        newDi.setId(id);
                        return newDi;
                    });

            try {
                di.setAmount(Float.parseFloat(amountStr));
            } catch (Exception e) {
                di.setAmount(null);
            }

            di.setAmountBackup(amountStr);
            di.setUnit(unit);

            drugIngredientService.save(di);
        }
    }

    private String extractLine(String block, String label) {
        Matcher m = Pattern.compile(label + "\\s*(.+)").matcher(block);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    protected void updateStorage(Drug drug, String block) {
        List<DrugStorageCondition> conditions = drugStorageConditionService.findByDrugId(drug.getId());
        Map<String, DrugStorageCondition.Category> labelMap = Map.of(
                "온도조건", DrugStorageCondition.Category.TEMPERATURE,
                "용기", DrugStorageCondition.Category.CONTAINER,
                "습도", DrugStorageCondition.Category.HUMID,
                "차광", DrugStorageCondition.Category.LIGHT
        );

        for (Map.Entry<String, DrugStorageCondition.Category> entry : labelMap.entrySet()) {
            String label = entry.getKey();
            DrugStorageCondition.Category category = entry.getValue();

            Matcher m = Pattern.compile("- " + label + ": (.+)").matcher(block);
            if (m.find()) {
                String value = m.group(1).trim();
                if (!value.isBlank() && !value.contains("정보 없음")) {
                    for (DrugStorageCondition cond : conditions) {
                        if (cond.getCategory() == category) {
                            cond.setValue(value);
                            cond.setActive(true);
                            drugStorageConditionService.save(cond);
                        }
                    }
                }
            }
        }
    }

    protected void updateForm(Drug drug, String block) {
        Matcher m = Pattern.compile("제형:\\s*(.+)").matcher(block);
        if (m.find()) {
            String form = m.group(1).trim();
            drug.setForm(form);
            drugService.save(drug);
        }
    }

    private Optional<Ingredient> matchIngredientByName(String rawName) {
        String norm = normalizeText(rawName);

        for (Ingredient ingr : ingredientService.findAll()) {
            String ingrName = ingr.getNameKr();
            String ingrNorm = normalizeText(ingrName);
            if (ingrName == null || rawName == null) continue;

            // 1. 완전 일치
            if (ingrName.equals(rawName)) return Optional.of(ingr);

            // 2. 정규화 후 일치
            if (ingrNorm.equals(norm)) return Optional.of(ingr);

            // 3. 괄호 제거 후 일치
            String noParen1 = removeParentheses(ingrName);
            String noParen2 = removeParentheses(rawName);
            if (noParen1.equals(noParen2)) return Optional.of(ingr);
        }

        return Optional.empty();
    }

    private String normalizeText(String text) {
        if (text == null) {
            System.out.println("[normalizeText] text is null!");
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replaceAll("[\\s\\u200B\\uFEFF]", "")
                .trim();
    }

    private String removeParentheses(String text) {
        if (text == null) {
            System.out.println("[removeParentheses] text is null!");
            return "";
        }
        return text.replaceAll("\\(.*?\\)", "").trim();
    }


    private DrugEffect findOrCreateEffect(Drug drug, DrugEffect.Type type) {
        return drug.getDrugEffects().stream()
                .filter(e -> e.getType() == type)
                .findFirst()
                .orElseGet(() -> {
                    DrugEffect e = new DrugEffect();
                    e.setDrug(drug);
                    e.setType(type);
                    e.setContent("");
                    return e;
                });
    }


}

