package com.oauth2.HealthSupplement.Import.SupplementImport.Service;

import com.oauth2.HealthSupplement.IntakeRequire.Entity.IntakeRequire;
import com.oauth2.HealthSupplement.IntakeRequire.Repository.IntakeRequireRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthIngredient;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntakeImportService {
    private final IntakeRequireRepository intakeRequireRepository;
    private final HealthIngredientRepository healthIngredientRepository;

    @Value("${supplement.intake}")
    private String intakeName;

    public void parseAndSave() throws IOException {
        FileInputStream fis = new FileInputStream(intakeName);
        Workbook workbook = new XSSFWorkbook(fis);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String nutrientName = sheet.getSheetName(); // 성분 이름 = 시트 이름

            // 성분 ID 가져오기
            if(nutrientName.equals("인")) continue;
            List<HealthIngredient> ingredient = healthIngredientRepository.findByNameContaining(removeLeadingParentheses(nutrientName));


            String currentGender = null;
            String currentAgeRange = null;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // 0번째 셀 = 성별, 1번째 셀 = 연령
                Cell genderCell = row.getCell(0);
                Cell ageCell = row.getCell(1);

                String genderRaw = getString(genderCell);
                String ageRangeRaw = getString(ageCell);

                if (genderRaw != null && !genderRaw.isEmpty()) currentGender = genderRaw;
                if (ageRangeRaw != null && !ageRangeRaw.isEmpty()) currentAgeRange = ageRangeRaw;

                if (currentGender == null || currentAgeRange == null) continue; // 완전한 행이 아님

                // 이후 나머지 값도 파싱
                Double recommend = getDouble(row.getCell(2));
                Double enough = getDouble(row.getCell(3));
                Double minimum = getDouble(row.getCell(4));
                Double maximum = getDouble(row.getCell(5));
                String unit = getString(row.getCell(6));

                IntakeRequire.Status status = mapToStatus(currentGender);
                for(HealthIngredient healthIngredient : ingredient) {
                    IntakeRequire entity = IntakeRequire.builder()
                            .healthIngredient(healthIngredient)
                            .ageRange(currentAgeRange)
                            .unit(unit)
                            .status(status)
                            .intakeRecommend(recommend)
                            .intakeEnough(enough)
                            .intakeMinimum(minimum)
                            .intakeMaximum(maximum)
                            .build();

                    intakeRequireRepository.save(entity);
                }
            }
        }

        workbook.close();
    }

    private String getString(Cell cell) {
        return (cell != null) ? cell.getStringCellValue().trim() : null;
    }

    private Double getDouble(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (Exception e) { yield null; }
            }
            default -> null;
        };
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

    private IntakeRequire.Status mapToStatus(String raw) {
        if (raw == null) return null;
        return switch (raw.trim()) {
            case "남자" -> IntakeRequire.Status.MALE;
            case "여자" -> IntakeRequire.Status.FEMALE;
            case "유아", "영아" -> IntakeRequire.Status.BABY;
            case "임산부" -> IntakeRequire.Status.PREGNANCY;
            case "수유부" -> IntakeRequire.Status.LACTATION;
            default -> throw new IllegalArgumentException("매핑 불가: " + raw);
        };
    }
}
