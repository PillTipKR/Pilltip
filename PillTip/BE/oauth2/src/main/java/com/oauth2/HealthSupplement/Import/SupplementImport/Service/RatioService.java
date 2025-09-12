package com.oauth2.HealthSupplement.Import.SupplementImport.Service;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementIngredient;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RatioService {

    private final HealthSupplementIngredientRepository healthSupplementIngredientRepository;

    @Value("${supplement.ratio}")
    private String ratio;

    public void processRatioFile() throws IOException {
        String allText = Files.readString(Path.of(ratio));
        Map<Long, Double> ratioMap = new HashMap<>();
        // 멀티라인 모드로 item_id: 기준 split
        String[] blocks = allText.split("(?m)(?=^item_id:)");

        for (String block : blocks) {
            String[] lines = block.trim().split("\\R"); // \R은 줄바꿈(\n 또는 \r\n) 대응
            Long itemId = null;
            Double ratio = null;

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("item_id:")) {
                    itemId = Long.parseLong(line.replace("item_id:", "").trim());
                } else if (line.startsWith("ratio:")) {
                    ratio = parseRatio(line.replace("ratio:", "").trim());
                }
            }

            if (itemId != null && ratio != null) {
                ratioMap.put(itemId, ratio);
            }
        }

        for (Map.Entry<Long, Double> entry : ratioMap.entrySet()) {
            long itemId = entry.getKey();
            double ratio = entry.getValue();
            List<HealthSupplementIngredient> healthSupplementIngredientList
                    = healthSupplementIngredientRepository.findBySupplementId(itemId);
            for(HealthSupplementIngredient healthSupplementIngredient : healthSupplementIngredientList) {
                double weight = healthSupplementIngredient.getAmount();
                double result = ratio * weight;
                healthSupplementIngredient.setAmount(result);
            }
            healthSupplementIngredientRepository.saveAll(healthSupplementIngredientList);
        }
    }

    private double parseRatio(String str) {
        if (str.contains("/")) {
            String[] parts = str.split("/");
            return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        } else {
            return Double.parseDouble(str.trim());
        }
    }
}
