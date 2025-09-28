package com.oauth2.AgenticAI.Util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.AgenticAI.Dto.DoseInfo.AgeSynonymCategory;
import com.oauth2.AgenticAI.Dto.DoseInfo.NormalizationResult;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgeNormalizer {

    private final Map<String, AgeSynonymCategory> synonymToCategoryMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 학년-나이 변환을 위한 정적 맵 (만 나이 기준)
    private static final Map<String, Map<Integer, Integer>> GRADE_TO_AGE_MAP = new HashMap<>();

    static {
        Map<Integer, Integer> elementary = Map.of(1, 7, 2, 8, 3, 9, 4, 10, 5, 11, 6, 12);
        GRADE_TO_AGE_MAP.put("초등학생", elementary);

        Map<Integer, Integer> middle = Map.of(1, 13, 2, 14, 3, 15);
        GRADE_TO_AGE_MAP.put("중학생", middle);
    }

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/age_sym.json")) {
            if (is == null) throw new IllegalStateException("age_sym.json 파일을 찾을 수 없습니다.");
            List<AgeSynonymCategory> categories = objectMapper.readValue(is, new TypeReference<>() {});
            for (AgeSynonymCategory category : categories) {
                synonymToCategoryMap.put(category.getCategory().toLowerCase(), category);
                for (String synonym : category.getSynonyms()) {
                    synonymToCategoryMap.put(synonym.toLowerCase(), category);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("AgeNormalizer 초기화 실패", e);
        }
    }

    public NormalizationResult normalize(String userInput, String contextCategory) {
        String lowerCaseInput = userInput.toLowerCase();

        // 1순위: 컨텍스트가 있는 학년/개월/나이 답변 처리
        if (contextCategory != null && !contextCategory.isBlank()) {
            // 학년 답변 처리 (예: "5학년")
            Matcher gradeMatcher = Pattern.compile("(\\d+)\\s*학년").matcher(userInput);
            if (gradeMatcher.find()) {
                int grade = Integer.parseInt(gradeMatcher.group(1));
                if (GRADE_TO_AGE_MAP.containsKey(contextCategory)) {
                    Integer age = GRADE_TO_AGE_MAP.get(contextCategory).get(grade);
                    if (age != null) {
                        return new NormalizationResult(age + "세", false, null, null);
                    }
                }
            }
            // 개월/나이 답변 처리 (예: "6개월", "5살") - 숫자만 있는 경우
            Matcher numberMatcher = Pattern.compile("(\\d+)\\s*(개월|살|세)?").matcher(userInput);
            if(numberMatcher.find()){
                return new NormalizationResult(numberMatcher.group(0), false, null, null);
            }
        }

        // 2순위: 초기 질문에서 명확한 나이/개월이 있는 경우
        Matcher specificAgeMatcher = Pattern.compile("(\\d+)\\s*(개월|살|세)").matcher(userInput);
        if (specificAgeMatcher.find()) {
            return new NormalizationResult(specificAgeMatcher.group(0), false, null, null);
        }

        // 3순위: 키워드 검색
        for (Map.Entry<String, AgeSynonymCategory> entry : synonymToCategoryMap.entrySet()) {
            if (lowerCaseInput.contains(entry.getKey())) {
                AgeSynonymCategory category = entry.getValue();
                return new NormalizationResult(
                        userInput,
                        category.isRequiresClarification(),
                        category.getClarificationQuestion(),
                        category.getCategory()
                );
            }
        }

        // 아무것도 해당하지 않을 경우
        return new NormalizationResult(userInput, false, null, null);
    }
}