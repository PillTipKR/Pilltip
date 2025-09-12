package com.oauth2.Drug.Import.DrugImport.Service;

import com.oauth2.Drug.DrugInfo.Domain.*;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Service.DrugEffectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PromptImporter {

    private final DrugRepository drugRepository;
    private final DrugEffectService drugEffectService;
    private final PromptDrugUpdater promptDrugUpdater;
    private final Logger logger = LoggerFactory.getLogger(PromptImporter.class);

    public void importPromptInfo(String filePath) {
        try {
            String fullText = Files.readString(Path.of(filePath));
            List<String> blocks = splitToDrugBlocks(fullText);

            for (String block : blocks) {
                String[] lines = block.split("\n");
                if (lines.length == 0 || !lines[0].startsWith("### ")) continue;

                String originalName = lines[0].substring(4).trim();
                Drug matchedDrug = matchDrugByNameVariants(originalName);
                if (matchedDrug == null) {
                    System.out.println("[매칭 실패] " + originalName);
                    continue;
                }

                Map<String, String> sectionMap = extractSections(block);
                promptDrugUpdater.updatePromptDataForDrug(matchedDrug, sectionMap);
            }
        } catch (IOException e) {
            logger.error("Error occurred in import drug file: {}", e.getMessage());
        }
    }

    private List<String> splitToDrugBlocks(String fullText) {
        List<String> blocks = new ArrayList<>();
        String[] lines = fullText.split("\n");

        List<String> currentBlock = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("### ")) {
                if (!currentBlock.isEmpty()) {
                    blocks.add(String.join("\n", currentBlock));
                    currentBlock.clear();
                }
            }

            if (!currentBlock.isEmpty() || line.startsWith("### ")) {
                currentBlock.add(line);
            }

            if (line.strip().equals("---")) {
                blocks.add(String.join("\n", currentBlock));
                currentBlock.clear();
            }
        }

        return blocks;
    }


    private String normalizeDrugName(String name) {
        name = Normalizer.normalize(name, Normalizer.Form.NFKC);
        name = name.replaceAll("[\\s\\u200B\\uFEFF]+", "");
        name = removeLeadingParentheses(name);
        name = removeSquareBrackets(name);
        return name.trim();
    }

    private String removeSquareBrackets(String name) {
        // 예: "약이름 [수출명: 뭐시기]" → "약이름"
        return name.replaceAll("\\[.*?]", "").trim();
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


    private Map<String, String> extractSections(String block) {
        Map<String, String> map = new HashMap<>();
        Matcher m = Pattern.compile("#### (\\d+)\\. (.+?)\\s*[\r\n]+").matcher(block);

        List<Integer> sectionIndices = new ArrayList<>();
        List<String> sectionLabels = new ArrayList<>();
        while (m.find()) {
            sectionIndices.add(m.start());
            sectionLabels.add(m.group(1));
        }

        sectionIndices.add(block.length());
        for (int i = 0; i < sectionLabels.size(); i++) {
            int start = sectionIndices.get(i);
            int end = sectionIndices.get(i + 1);
            String content = block.substring(start, end);
            map.put(sectionLabels.get(i), content);
        }
        return map;
    }


    protected Drug matchDrugByNameVariants(String name) {
        // 1단계: 원래 이름 기준 매칭
        List<Drug> match = drugRepository.findByNameContaining(name);
        if(!match.isEmpty()) return match.get(0);

        // 2단계: 괄호 제거 후 매칭
        String noParen = removeLeadingParentheses(name);
        match = drugRepository.findByNameContaining(noParen);
        if(!match.isEmpty()) return match.get(0);

        // 3단계: 정규화 후 공백 제거 매칭
        String normalized = normalizeDrugName(noParen);
        match = drugRepository.findByNameContaining(normalized);
        if(!match.isEmpty()) return match.get(0);

        return null;
    }

    private String cleanAllFormatting(String input) {
        if (input == null) return "";

        String result = input;

        // 1. 역슬래시 제거
        result = result.replaceAll("\\\\", "");

        // 2. HTML 태그 제거
        result = result.replaceAll("<[^>]*>", "");

        // 3. HTML 특수문자 제거 (기본 escape 문자)
        result = result.replaceAll("&[a-zA-Z]+;", "");

        // 4. 마크다운 특수문자 제거
        String[] markdownSymbols = {
                "\\*\\*",  // **
                "~~",      // ~~
                "`+",      // ` or ```
                "#+",      // #, ## 등
                "^>\\s*",  // 인용문 '>'는 문장 앞에 오는 경우만 제거 (문장 내부 > 는 보존)
                "^\\-\\s*", // 리스트 '-' 문장 앞에서만 제거
                "^\\+\\s*", // 리스트 '+' 문장 앞에서만 제거
                "^\\d+\\.\\s*", // 숫자 리스트 항목: '1. ', '2. ' 등
        };

        for (String symbol : markdownSymbols) {
            result = result.replaceAll(symbol, "");
        }

        return result;
    }


    @Transactional
    public void importPromptIngCaution(String filePath) throws IOException {
        String fileContent = Files.readString(Path.of(filePath));
        String[] blocks = fileContent.split("===+");
        List<DrugEffect> cautions = new ArrayList<>();
        for (int i = 1; i < blocks.length; i += 2) {
            String ingredientName = blocks[i].trim(); // ex. "로페라미드"
            String cautionText = blocks[i + 1].trim();

            // 성분명이 포함된 모든 약물 검색
            List<Drug> matchedDrugs = drugRepository.findByNameContaining(ingredientName);
            if (matchedDrugs.isEmpty()) {
                System.out.println("일치하는 약물이 없음: " + ingredientName);
                continue;
            }

            for (Drug drug : matchedDrugs) {
                Optional<DrugEffect> cautionOpt = drug.getDrugEffects().stream()
                        .filter(e -> e.getType() == DrugEffect.Type.CAUTION)
                        .findFirst();

                DrugEffect caution;
                if (cautionOpt.isPresent()) {
                    caution = cautionOpt.get();
                    cautionText = cleanAllFormatting(cautionText);
                    caution.setContent(cautionText);
                } else {
                    // CAUTION 타입이 없으면 새로 생성
                    caution = new DrugEffect();
                    caution.setDrug(drug);
                    caution.setType(DrugEffect.Type.CAUTION);
                    caution.setContent(cautionText);
                }

                cautions.add(caution);
                System.out.println("저장 완료: " + drug.getName());
            }
        }
        drugEffectService.saveAll(cautions);
    }

    @Transactional
    public void importPromptCaution(String filePath) {
        try {
            String fullText = Files.readString(Path.of(filePath));
            String[] blocks = fullText.split("=== "); // "=== 약이름 주의사항 ===" 기준으로 분할
            List<DrugEffect> cautions = new ArrayList<>();

            for (String block : blocks) {
                block = block.trim();
                if (block.isEmpty()) continue;

                // 1. 약 이름과 주의사항 분리
                int titleEndIdx = block.indexOf("주의사항 ===");
                if (titleEndIdx == -1) continue;

                String drugName = block.substring(0, titleEndIdx).trim();
                String cautionText = block.substring(titleEndIdx + "주의사항 ===".length()).trim();
                cautionText = cleanAllFormatting(cautionText);
                // 2. 주의사항이 무의미한 경우 스킵
                if (cautionText.contains("유효한 주의사항 정보가 없습니다.")) {
                    //System.out.println("스킵됨 (주의사항 없음): " + drugName);
                    return;
                }

                // 3. 약 찾기
                Drug drugOpt = matchDrugByNameVariants(drugName);
                if (drugOpt == null) {
                    System.out.println("해당 약을 찾을 수 없음: " + drugName);
                    return;
                }

                // 4. 해당 약의 CAUTION 타입 주의사항 찾기
                Optional<DrugEffect> cautionOpt = drugOpt.getDrugEffects().stream()
                        .filter(e -> e.getType() == DrugEffect.Type.CAUTION)
                        .findFirst();

                if (cautionOpt.isPresent()) {
                    DrugEffect caution = cautionOpt.get();
                    caution.setContent(cautionText);
                    cautions.add(caution);
                } else {
                    System.out.println("주의사항 항목이 존재하지 않음 (신규 생성 필요?): " + drugName);
                }
            }
            drugEffectService.saveAll(cautions);
        } catch (IOException e) {
            logger.error("Error occurred in import caution file: {}", e.getMessage());
        }
    }

}
