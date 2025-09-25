package com.oauth2.Drug.Import.DURImport.Service;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrugInteractionService {

    private final SubjectInteractionRepository subjectInteractionRepository;
    private final DrugRepository drugRepository;

    @Value("${interactionFile1}")
    private String interactionFile1;

    @Value("${interactionFile2}")
    private String interactionFile2;

    public void loadAll() throws IOException {
        parseInteractionCautions(interactionFile1);
        parseInteractionCautions(interactionFile2);

    }

    private void parseInteractionCautions(String path) throws IOException {
        String content = Files.readString(Paths.get(path));
        String[] lines = content.split("\n"); // 전체 내용에서 라인별로 나누기

        String productName1 = null;
        String productName2 = null;
        String reason = "";
        String note = "";

        for (String line : lines) {
            line = line.trim(); // 공백 제거

            // 제품명 추출
            if (line.startsWith("제품명: ")) {
                productName1 = line.replace("제품명: ", "").trim();
            }
            else if (line.startsWith("병용 제품명: ")) {
                productName2 = line.replace("병용 제품명: ", "").trim();
            }
            // 임부금기내용
            else if (line.contains("금기내용:")) {
                reason = line.substring(line.indexOf("금기내용:") + "금기내용:".length()).trim();
            }
            // 비고
            else if (line.startsWith("비고:")) {
                note = line.replace("비고:", "").trim();
            }
            // 구분선(또는 다음 데이터 시작)
            else if (line.startsWith("==================")) {
                if ((productName1 != null && !productName1.isBlank())
                        && (productName2 != null && !productName2.isBlank())) {
                    saveDrugInteraction(productName1, productName2, reason, note);
                }

                // 초기화
                productName1 = productName2 = reason = note = null;
            }
        }

        // 마지막 데이터 처리
        if ((productName1 != null && !productName1.isBlank())
                && (productName2 != null && !productName2.isBlank())) {
            saveDrugInteraction(productName1, productName2, reason, note);
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

    private void saveDrugInteraction(String pName1, String pName2, String reason, String note){
        pName1 = removeLeadingParentheses(pName1);
        pName2 = removeLeadingParentheses(pName2);
        List<Drug> idList1 = drugRepository.findByNameContaining(pName1);
        List<Drug> idList2 = drugRepository.findByNameContaining(pName2);

        if(!idList1.isEmpty() && !idList2.isEmpty()) {
            Drug id1 = idList1.get(0);
            Drug id2 = idList2.get(0);
            List<SubjectInteraction> drugInter =
                    subjectInteractionRepository.findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(id1.getId(), DurType.DRUG,id2.getId(),DurType.DRUG);
            if(drugInter.isEmpty()) {
                SubjectInteraction subjectInteraction = new SubjectInteraction();
                subjectInteraction.setSubjectId1(id1.getId());
                subjectInteraction.setSubjectId2(id2.getId());
                subjectInteraction.setDurtype1(DurType.DRUG);
                subjectInteraction.setDurtype2(DurType.DRUG);
                subjectInteraction.setReason(reason);
                subjectInteraction.setNote(note);

                save(subjectInteraction);
            }

        }else {
            System.out.println("약품명 [" + pName1 + " 혹은 " + pName2 + "]  을(를) 찾을 수 없습니다.");
        }
    }

    public SubjectInteraction save(SubjectInteraction subjectInteraction) {
        return subjectInteractionRepository.save(subjectInteraction);
    }
    public void delete(Long id) {
        subjectInteractionRepository.deleteById(id);
    }
    public SubjectInteraction findById(Long id) {
        return subjectInteractionRepository.findById(id).orElse(null);
    }
}
