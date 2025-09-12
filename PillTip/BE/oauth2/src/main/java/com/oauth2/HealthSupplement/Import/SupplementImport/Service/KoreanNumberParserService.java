package com.oauth2.HealthSupplement.Import.SupplementImport.Service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KoreanNumberParserService {

    private static final Map<String, Long> numberMap = new HashMap<>();
    private static final Map<String, Long> unitMap = new LinkedHashMap<>();

    static {
        numberMap.put("영", 0L); numberMap.put("일", 1L); numberMap.put("이", 2L);
        numberMap.put("삼", 3L); numberMap.put("사", 4L); numberMap.put("오", 5L);
        numberMap.put("육", 6L); numberMap.put("칠", 7L); numberMap.put("팔", 8L); numberMap.put("구", 9L);

        unitMap.put("조", 1_0000_0000_0000L);
        unitMap.put("억", 1_0000_0000L);
        unitMap.put("만", 1_0000L);
        unitMap.put("", 1L); // 1의 자리
    }

    public long parseKoreanNumber(String input) {
        input = input.replaceAll("[,\\s]", ""); // 쉼표, 공백 제거
        StringBuilder numericBuffer = new StringBuilder();
        long total = 0;

        for (Map.Entry<String, Long> entry : unitMap.entrySet()) {
            String unit = entry.getKey();
            long unitValue = entry.getValue();

            int idx = input.indexOf(unit);
            if (idx >= 0) {
                String part = input.substring(0, idx);
                input = input.substring(idx + unit.length());
                long parsed = parseChunk(part);
                total += parsed * unitValue;
            }
        }

        // 남은 숫자 처리 (단위 없이 숫자만 있는 경우)
        if (!input.isEmpty()) {
            total += parseChunk(input);
        }

        return total;
    }

    private long parseChunk(String chunk) {
        if (chunk.matches("\\d+")) {
            return Long.parseLong(chunk);
        }

        long result = 0;
        long temp = 0;

        for (int i = 0; i < chunk.length(); i++) {
            String c = String.valueOf(chunk.charAt(i));
            if (numberMap.containsKey(c)) {
                temp = numberMap.get(c);
            } else if (c.equals("천")) {
                result += (temp == 0 ? 1 : temp) * 1000;
                temp = 0;
            } else if (c.equals("백")) {
                result += (temp == 0 ? 1 : temp) * 100;
                temp = 0;
            } else if (c.equals("십")) {
                result += (temp == 0 ? 1 : temp) * 10;
                temp = 0;
            }
        }

        result += temp;
        return result;
    }
}

