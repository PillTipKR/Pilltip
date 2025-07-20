package com.oauth2.HealthSupplement.Service;

import com.oauth2.HealthSupplement.Entity.HealthIngredient;
import com.oauth2.HealthSupplement.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.Entity.SupplementIngredient;
import com.oauth2.HealthSupplement.Repository.HealthIngredientRepository;
import com.oauth2.HealthSupplement.Repository.SupplementIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SupplementParsingService {

    private final HealthIngredientRepository ingredientRepository;
    private final SupplementIngredientRepository supplementIngredientRepository;

    private static final Pattern[] patterns = new Pattern[] {

            // ★ 우선순위 최상단에 삽입
            Pattern.compile("(?<name>.+?)\\(\\s*%\\s*\\)\\s*[:：]?\\s*표시량\\s*\\(\\s*(?<amount>[\\d\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUugIU]+)\\s*\\)(/[^)]*)?\\)??\\s*의\\s*(?<min>\\d+)%[~∼～－-](?<max>\\d+)%"),

            // 16. 성분명(단위) : 표시량(수치)의 NN~NN%
            Pattern.compile("(?<name>.+?)\\((?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%RE]+)\\)\\s*[:：]?\\s*표시량\\s*\\(\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*\\)\\s*의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 패턴 17. "표시량의 80~120% (표시량: 20 mg )"
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%\\s*\\(표시량[:：]?\\s*(?<amount>[\\d\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUugIU]+)\\s*\\)"),

            // 패턴 18. "마그네슘 : 105 mg (표시량의 80~150%)"
            Pattern.compile("(?<name>.+?)[:：]\\s*(?<amount>[\\d\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUugIU]+)\\s*\\(\\s*표시량의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%\\s*\\)"),

            // 패턴 19. "진세노사이드Rg1,Rb1,Rg3의 합 : 표시량(3.0 mg))의 80%이상"
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량\\s*\\(\\s*(?<amount>[\\d\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUugIU]+)\\s*\\)\\)*\\s*의\\s*(?<min>\\d+)%\\s*이상"),

            // 1. 성분명(단위) : 표시량(수치)의 NN~NN%
            Pattern.compile("(?<name>.+?)\\((?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)\\s*[:：]?\\s*표시량\\s*\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\)?\\s*의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 2. 표시량(수치 단위)의 NN~NN%
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량\\s*\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)?\\s*\\)?\\s*(?:의)?\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 3. 표시량(수치 단위)의 NN% 이상
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량\\s*\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)?\\s*(?:의)?\\s*(?<min>\\d+)%\\s*이상"),

            // 4. 성분명(단위): 표시량 수치의 NN~NN%
            Pattern.compile("(?<name>.+?)\\((?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)[:：]?\\s*표시량\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 5. 성분명: (수치 단위)의 NN~NN%
            Pattern.compile("(?<name>.+?)[:：]?\\s*\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)?\\s*의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 6. 성분명: 표시량의(수치 단위)의 NN~NN%
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량의\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)?\\s*의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%"),

            // 7. 단순 이상 조건 (e.g., CFU/g 이상)
            Pattern.compile("(?<name>.+?)[:：]?\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)(/\\s*[\\d\\.]+\\s*(mg|g|ml|㎎|㎖))?\\s*(이상|초과|이하)?"),

            // 8. "성분명: 표시량: 수치 단위"
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량[:：]\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)"),

            // 9. 표기만 된 경우 (e.g., 표시량(130 mg))
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량\\s*\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)\\)?"),

            // 10. 성분명: 수치 단위 구조 (e.g., 비타민B12: 2.4 ug)
            Pattern.compile("(?<name>.+?)[:：]?\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuαTEugIU%NE]+)"),

            // 11. 표시량의 NN~NN% (앞에 수치가 있는 경우 대응)
            Pattern.compile("(?<name>.+?)[:：]?\\s*\\(표시량의\\s*(?<min>\\d+)[~∼～－-](?<max>\\d+)%\\)"),

            // 12. CFU 구조 특화
            Pattern.compile("(?<name>.+?)[:：]?\\s*(?<amount>[\\d,\\.Ee^×x+-]+)\\s*(CFU|CFUs)\\s*/\\s*[^\\s]+\\s*(이상|초과)?"),

            // "표시량(수치 단위) 이상" 또는 "표시량(수치) 단위 이상"
            Pattern.compile("(?<name>.+?)[:：]?\\s*표시량\\(?(?<amount>[\\d,\\.Ee^×x+-]+)\\)?\\s*(?<unit>[a-zA-Zμ㎍㎎CFUcfuugIU]+)\\)?\\s*(이상|초과)")

    };








    public void parseBaseStandard(String baseStandard, HealthSupplement supplement) {
        if (baseStandard == null) return;

        String[] lines = baseStandard.split("\\r?\\n");
        List<String> mergedLines = new ArrayList<>();

        // 1. 줄 병합 및 정리
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(".*성\\s*상.*")) continue;
            if (lines[i].matches(".*붕\\s*해.*")) continue;
            if (lines[i].matches("대장균군")) continue;
            if (lines[i].matches("세균")) continue;
            if ((lines[i].contains("이하") || lines[i].contains("미만") || lines[i].contains("초과"))
                    && !lines[i].contains("이상")) {
                continue;
            }
            String current = lines[i].trim()
                    .replaceAll("^[0-9]+[.)]?\\s*", "")
                    .replaceAll("\\s*[①②③④⑤⑥⑦⑧⑨⑩⑾⑿⒀⒁⒂⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽]\\s*", "")
                    .replaceAll("^[\\(\\[]\\d+[\\)\\]]", "")
                    .replaceAll("\\*", "")
                    .replaceAll("\\-", "")
                    .replaceAll("\\s+", " ")
                    .replaceAll("\\+", ", ")
                    .replaceAll("총\\(\\)", "")
                    .replaceAll("표시량\\((\\d{2})(\\d{2})\\s*\\)%", "표시량($1~$2)%")
                    .replaceAll("㎎", "mg").replaceAll("㎍", "ug").replaceAll("μg", "ug")
                    .replaceAll("＋", "+").replaceAll("：", ":").replaceAll("×", "x")
                    .replaceAll(">함량", "")
                    .replaceAll("으로서", "")
                    .replaceAll("로서", "")
                    .replaceAll("표시량\\s+([\\d,\\.]+)(\\([^)]*\\))?", "표시량($1)")
                    .replaceAll("(/\\s*\\d+[.,\\d]*\\s*(mg|ug|g|ml|CFU|IU|NE|αTE))", "")
                    .replaceAll("표시량\\(([^\\)]+)\\s*\\)([a-zA-Zμ㎍㎎CFUcfuugIU]+)\\)", "표시량($1 $2)")
                    .replaceAll("\\((\\d[\\d,\\.Ee^x]+)\\s*([a-zA-Zμ㎍㎎CFUugIU]+)\\)", "($1 $2)")
                    .replaceAll("\\((mg|ug|g|ml|CFU|IU)(\\s*)?(mg|ug|g|ml|CFU|IU)\\)", "($1)")
                    .replaceAll("\\((\\d[\\d,\\.Ee^x]+)\\([^)]*\\)\\s*([^)]+)\\)", "($1 $2)")
                    .replaceAll("표시량\\s*[:：]", "표시량:")
                    .replaceAll("\\s+", " ")
                    .replaceAll("(80)(1[0-9]{2})%", "$1~$2%")
                    .replaceAll("(\\d+)\\s*이상\\s*(\\d+)%\\s*이하", "$1~$2%")
                    .replaceAll("(?i)(\\d+(\\.\\d+)?)\\s*[x×X]\\s*10\\^?(\\d+)", "$1E$3")
                    .replaceAll("\\((\\d[\\d,]*)?(억|조|천|만|백만|십억)[^)]*\\)", "")
                    .replaceAll("표시량[{\\[\\(]?(\\d[\\d,\\.Ee]+)[\\)\\]}]?\\s*(CFU|IU|mg|ug|g)?", "표시량($1 $2)");
            if (i + 1 < lines.length && lines[i + 1].matches("^\\s+")) {
                String next = lines[i + 1].trim().replaceAll("[①②③④⑤⑥⑦⑧⑨⑩](\\s*)", "");
                current += " " + next;
                i++;
            }
            mergedLines.add(current);
        }

        for (String line : mergedLines) {
            System.out.println("line: " + line);
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group("name").replaceAll("^[-\\d.\s]*", "").trim();
                    double amount = parseNumber(matcher.group("amount"));
                    String unit = matcher.group("unit");
                    Double min = null;
                    Double max = null;

                    String minStr = safeGroup(matcher, "min");
                    if (minStr != null) {
                        min = Double.parseDouble(minStr) / 100.0;
                    }

                    String maxStr = safeGroup(matcher, "max");
                    if (maxStr != null) {
                        max = Double.parseDouble(maxStr) / 100.0;
                    }
                    // 2. 시험 관련 용어 필터링
                    if (line.matches(".*(시험방법|시험법|공전|기준|규격|시험항목|분석법).*")) {
                        continue;
                    }
                    String normal = normalizeName(name);
                    // 이후 로직에서 min, max가 null인지 체크
                    System.out.printf("성분: %s / 표시량: %s / 단위: %s / 범위: %.2f ~ %.2f\n",
                            normal, amount, unit, min != null ? min : -1, max != null ? max : -1
                    );
                    HealthIngredient ingredient = ingredientRepository.findByNameAndUnit(normal, unit)
                            .orElseGet(() -> ingredientRepository.save(
                                    HealthIngredient.builder().name(normal).unit(unit).build()
                            ));

                    supplementIngredientRepository.save(SupplementIngredient.builder()
                            .supplement(supplement)
                            .ingredient(ingredient)
                            .amount(amount)
                            .minRatio(min)
                            .maxRatio(max)
                            .build());
                    break;
                }
            }
        }
    }

    private String safeGroup(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }

    private double parseNumber(String raw) {
        try {
            raw = raw
                    .replaceAll(",", "")
                    .replaceAll("[^\\d.Ee+-]", "")  // 숫자/과학표기법만 남김
                    .replaceAll("[×xX*]\\s*10\\^?(\\d+)", "E$1")
                    .replaceAll("e", "E")
                    .trim();
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return 0.0;
        }
    }

    String normalizeName(String rawName) {
        return rawName
                // 진세노사이드 계열 통일
                .replaceAll("진세노사이드\\s*Rg1\\s*[+·,]\\s*Rb1\\s*[+·,]\\s*Rg3의?합", "진세노사이드 Rg1, Rb1 및 Rg3의 합")
                .replaceAll("진세노사이드Rg1\\+Rb1\\+Rg3의?합", "진세노사이드 Rg1, Rb1 및 Rg3의 합")
                .replaceAll("진세노사이드\\s*Rg1[,]?\\s*Rb1\\s*및\\s*Rg3의?합", "진세노사이드 Rg1, Rb1 및 Rg3의 합")
                .replaceAll("진세노사이드.*Rg1.*Rb1.*Rg3.*합", "진세노사이드 Rg1, Rb1 및 Rg3의 합")

                .replaceAll("\\(.*?\\)", "")  // 괄호 내용 전체 제거

                // 표시량, 총 등 의미 없는 접두어 제거
                .replaceAll("표시량[:：]?", "")
                .replaceAll("총[:：]?", "")

                // 단위 제거
                .replaceAll("\\b(mg|ug|g|ml|CFU|IU|NE|αTE|%)\\b", "")

                // 구두점/쉼표 등 정리
                .replaceAll("의", "")
                .replaceAll("[:：]", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("정제", "")
                .replaceAll("\\s*\\.","")
                .replaceAll("\\s+", " ")
                .trim();
    }




}


