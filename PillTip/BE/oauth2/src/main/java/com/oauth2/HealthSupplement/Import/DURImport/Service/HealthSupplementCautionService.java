package com.oauth2.HealthSupplement.Import.DURImport.Service;

import com.oauth2.HealthSupplement.DUR.Entity.HealthSupplementCaution;
import com.oauth2.HealthSupplement.DUR.Repository.HealthSupplementCautionRepository;
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

@Service
@RequiredArgsConstructor
public class HealthSupplementCautionService {

    private final HealthSupplementRepository healthSupplementRepository;
    private final HealthSupplementCautionRepository healthSupplementCautionRepository;

    @Value("${supplement.dur}")
    private String dur;

    private HealthSupplementCaution.ConditionType convertConditionType(String type) {
        return switch (type.trim()) {
            case "임신"     -> HealthSupplementCaution.ConditionType.PREGNANCY;
            case "소아"     -> HealthSupplementCaution.ConditionType.AGE;
            case "노인"     -> HealthSupplementCaution.ConditionType.ELDER;
            case "수유"   -> HealthSupplementCaution.ConditionType.LACTATION;
            default -> throw new IllegalArgumentException("지원되지 않는 유형: " + type);
        };
    }

    public void load() throws IOException {
        parseCautions();
    }

    public void parseCautions() throws IOException {

        try (
                Reader reader = new FileReader(dur);
                CSVParser csvParser = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .withAllowMissingColumnNames()
                        .withTrim()
                        .parse(reader)
        ) {
            for (CSVRecord record : csvParser) {
                String rawMatrl = record.get(0).trim();       // 첫 번째 컬럼
                String[] list = record.get(4)
                        .replaceAll("\\s+", "").split(",");    // "임신,수유,소아,노인"
                for(String dur : list) {
                    if(dur.isEmpty()) continue;
                    saveCaution(rawMatrl, convertConditionType(dur));
                }

            }
        }
    }

    private void saveCaution(String rawMatrl, HealthSupplementCaution.ConditionType conditionTypeStr) {

        List<Long> supplements = healthSupplementRepository.findHealthSupplementsByRawMaterial(rawMatrl);
        for(Long id : supplements) {
            if(healthSupplementCautionRepository.existsBySupplementIdAndConditionType(id,conditionTypeStr)) continue;
            HealthSupplementCaution healthSupplementCaution = new HealthSupplementCaution();
            healthSupplementCaution.setSupplementId(id);
            healthSupplementCaution.setConditionType(conditionTypeStr);
            healthSupplementCautionRepository.save(healthSupplementCaution);
        }
    }
}
