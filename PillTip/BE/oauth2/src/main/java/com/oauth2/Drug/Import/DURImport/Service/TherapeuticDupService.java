package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DrugTherapeuticDup;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DUR.Repository.DrugTherapeuticDupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TherapeuticDupService {

    @Value("${therFile}")
    private String therFile;

    private final DrugRepository drugRepository;
    private final DrugTherapeuticDupRepository drugTherapeuticDupRepository;

    public void parseAndSaveTherapeuticDup() throws IOException {
        String content = Files.readString(Paths.get(therFile));
        String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기

        String productName = null;
        String className = null; // 분류명
        String category = null;  // 효능군명
        String note = null;
        String remark = null;

        for (String line : lines) {
            line = line.trim(); // 공백 제거
            if (line.startsWith("제품명: ")) {
                productName = line.replace("제품명: ", "").trim();
            } else if (line.startsWith("분류명: ")) {
                className = line.replace("분류명: ", "").trim(); // 분류명 → className
            } else if (line.startsWith("효능군명: ")) {
                category = line.replace("효능군명: ", "").trim(); // 효능군명 → category
            } else if (line.startsWith("효능군중복내용: ")) {
                note = line.replace("효능군중복내용: ", "").trim();
            } else if (line.startsWith("비고: ")) {
                remark = line.replace("비고: ", "").trim();
            } else if (line.startsWith("===")) {
                if (productName != null && className != null && note != null && category != null) {
                    saveTherapeuticDup(productName, className, category, note, remark);
                    productName = className = category = note = remark = null;
                }
            }
        }

        // 마지막 항목 저장
        if (productName != null && className != null && note != null && category != null) {
            saveTherapeuticDup(productName, className, category, note, remark);
        }

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

    private void saveTherapeuticDup(String productName, String className, String category, String note, String remark) {
        productName = removeLeadingParentheses(productName);
        List<Drug> drugList = drugRepository.findByNameContaining(productName);
        if (!drugList.isEmpty()) {
            Drug drug = drugList.get(0);
            DrugTherapeuticDup dup = new DrugTherapeuticDup();
            dup.setCategory(category);       // 효능군명 → category
            dup.setClassName(className);     // 분류명 → className
            dup.setNote(note);
            dup.setRemark(remark);
            dup.setDrugId(drug.getId());

            drugTherapeuticDupRepository.save(dup);
        } else {
            System.out.println("약품명 [" + productName + "] 에 해당하는 drugId를 찾을 수 없습니다.");
        }
    }

}
