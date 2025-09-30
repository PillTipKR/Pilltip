package com.oauth2.AgenticAI.Tool;

import com.oauth2.AgenticAI.Service.RagSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DoseInfoTool {


    private final RagSearchService search;

    @Tool(name = "DoseInfoTool", description = "특정 '영양소/제품'과 '나이'에 대한 섭취량 정보를 제공합니다.")
    public Map<String,Object> run(
            @P(value = "nutrient") String nutrient,
            @P(value = "age") String age,
            @P(value = "gender") String gender) {

        System.out.println(nutrient+" "+age+" "+gender);
        // 이 Tool은 AgeNormalizer를 직접 호출하기보다,
        // Orchestrator가 정규화를 마친 뒤 얻은 명확한 'nutrient'와 'age'를 받는 역할에 집중합니다.

        // age 문자열("11세", "6개월")을 개월 수로 변환하는 로직이 필요합니다.
        Integer ageInMonths = convertAgeToMonths(age); // 아래에 예시 헬퍼 메서드
        String normalizedGender = normalizeGender(gender);
        // RagSearchService는 이제 nutrient와 ageInMonths를 받도록 수정됩니다.
        var docs = search.searchDose(nutrient, 5, ageInMonths,normalizedGender);

        if (docs.isEmpty()) {
            return Map.of(); // 문서가 없으면 빈 Map 반환
        }

        var doc = docs.get(0); // 첫 번째 문서만 사용

        assert doc.getText() != null;
        return Map.of(
                "id", doc.getId(),
                "age", age,
                "text", doc.getText(),
                "meta", doc.getMetadata()
        );
    }

    private Integer convertAgeToMonths(String age) {
        if (age == null || age.isBlank()) {
            return null;
        }

        String trimmedAge = age.trim();

        // 1. "개월" 단위가 있는지 먼저 체크 (가장 구체적이므로 우선순위 높음)
        Pattern monthPattern = Pattern.compile("(\\d+)\\s*개월");
        Matcher monthMatcher = monthPattern.matcher(trimmedAge);
        if (monthMatcher.find()) {
            try {
                return Integer.parseInt(monthMatcher.group(1));
            } catch (NumberFormatException e) {
                // 숫자가 너무 크거나 할 경우, 파싱 실패
                return null;
            }
        }

        // 2. "세" 또는 "살" 단위가 있는지 체크
        Pattern yearPattern = Pattern.compile("(\\d+)\\s*(세|살)");
        Matcher yearMatcher = yearPattern.matcher(trimmedAge);
        if (yearMatcher.find()) {
            try {
                return Integer.parseInt(yearMatcher.group(1)) * 12;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // 3. 단위 없이 숫자만 있는 경우 (예: "10") -> '세'로 간주
        try {
            return Integer.parseInt(trimmedAge) * 12;
        } catch (NumberFormatException e) {
            // 숫자로 변환할 수 없는 문자열이면 null 반환
            return null;
        }
    }

    private String normalizeGender(String gender) {
        String[] male = {"아들","도련님","남자","남성","남아","남편","삼촌","아빠"};
        String[] female = {"딸","공주님","여자","여성","여아","아내","와이프","엄마"};
        if(Arrays.asList(male).contains(gender)) return "남자";
        else if(Arrays.asList(female).contains(gender)) return "여자";
        else return gender;
    }
}
