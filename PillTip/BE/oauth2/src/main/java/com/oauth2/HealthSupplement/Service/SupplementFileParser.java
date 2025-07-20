package com.oauth2.HealthSupplement.Service;

import com.oauth2.HealthSupplement.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.Repository.HealthSupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplementFileParser {

    private final HealthSupplementRepository healthSupplementRepository;
    private final SupplementParsingService supplementParsingService;

    public void importFromFile(String filePath) throws IOException {
        String allText = Files.readString(Path.of(filePath));

        // 멀티라인 모드로 ENTRPS: 기준 split
        String[] blocks = allText.split("(?m)(?=^ENTRPS:)");

        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                List<String> blockLines = Arrays.asList(block.split("\\r?\\n"));
                parseSingleBlock(blockLines);
            }
        }
    }


    private static final Set<String> FIXED_KEYS = Set.of(
            "ENTRPS", "PRDUCT", "STTEMNT_NO", "REGIST_DT", "DISTB_PD",
            "SUNGSANG", "SRV_USE", "PRSRV_PD", "INTAKE_HINT1", "MAIN_FNCTN", "BASE_STANDARD"
    );

    private void parseSingleBlock(List<String> blockLines) {
        Map<String, String> fields = new LinkedHashMap<>();
        StringBuilder currentValue = new StringBuilder();
        String currentKey = null;

        for (String line : blockLines) {
            String trimmed = line.trim();

            // 키워드에 해당하는 줄이면 새 항목 시작
            int idx = trimmed.indexOf(":");
            if (idx > 0) {
                String possibleKey = trimmed.substring(0, idx).trim();
                if (FIXED_KEYS.contains(possibleKey)) {
                    // 이전 키 저장
                    if (currentKey != null) {
                        fields.put(currentKey, currentValue.toString().trim());
                    }

                    // 새 키 설정
                    currentKey = possibleKey;
                    currentValue = new StringBuilder(trimmed.substring(idx + 1).trim());
                    continue;
                }
            }

            // 현재 항목의 연속된 줄
            if (currentKey != null) {
                currentValue.append("\n").append(trimmed);
            }
        }

        if (currentKey != null) {
            fields.put(currentKey, currentValue.toString().trim());
        }

        saveToDatabase(fields);
    }


    private void saveToDatabase(Map<String, String> fields) {
        // HealthSupplement 저장
        HealthSupplement supplement = HealthSupplement.builder()
                .enterprise(fields.get("ENTRPS"))
                .productName(fields.get("PRDUCT"))
                .statementNo(fields.get("STTEMNT_NO"))
                .registerDate(fields.get("REGIST_DT"))
                .distributionPeriod(fields.get("DISTB_PD"))
                .appearance(fields.get("SUNGSANG"))
                .servingMethod(fields.get("SRV_USE"))
                .preservation(fields.get("PRSRV_PD"))
                .intakeCaution(fields.get("INTAKE_HINT1"))
                .mainFunction(fields.get("MAIN_FNCTN"))
                .build();

        supplement = healthSupplementRepository.save(supplement);

        // BASE_STANDARD에서 성분만 파싱
        supplementParsingService.parseBaseStandard(fields.get("BASE_STANDARD"), supplement);
    }
}
