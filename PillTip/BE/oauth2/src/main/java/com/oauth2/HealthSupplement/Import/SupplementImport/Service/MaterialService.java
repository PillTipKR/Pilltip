package com.oauth2.HealthSupplement.Import.SupplementImport.Service;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementMat;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementMatRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

@Service
@RequiredArgsConstructor
public class MaterialService {
    private final HealthSupplementMatRepository healthSupplementMatRepository;

    @Value("${supplement.interaction}")
    private String interaction;

    public void importRaw() throws IOException {
        parseIngredientInteraction(interaction);
    }

    private void parseIngredientInteraction(String filePath) throws IOException {

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
                String rawMtrl = record.get(1).trim();       // 첫 번째 컬럼
                if(!healthSupplementMatRepository.findByMaterialName(rawMtrl).isEmpty()) continue;
                HealthSupplementMat hsm = new HealthSupplementMat();
                hsm.setMaterialName(rawMtrl);
                healthSupplementMatRepository.save(hsm);
            }
        }
    }

}
