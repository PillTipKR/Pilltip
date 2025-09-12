package com.oauth2.HealthSupplement.Import.SupplementImport.Service;

import com.oauth2.HealthSupplement.SupplementInfo.Entity.*;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SupplementFileParser {

    private final HealthSupplementRepository healthSupplementRepository;
    private final HealthSupplementEffectRepository effectRepository;
    private final KoreanNumberParserService koreanNumberParserService;
    private final HealthIngredientRepository healthIngredientRepository;
    private final HealthSupplementIngredientRepository ingredientRepository;
    private final HealthSupplementStorageConditionRepository healthSupplementStorageConditionRepository;

    private static final Set<String> FIXED_KEYS = Set.of(
            "BSSH_NM","PRDLST_NM", "LCNS_NO", "POG_DAYCNT", "PRDT_SHAP_CD_NM",
            "DISPOS", "NTK_MTHD", "PRIMARY_FNCLTY", "IFTKN_ATNT_MATR_CN", "CSTDY_MTHD", "STDR_STND",
            "LAST_UPDT_DTM", "INDUTY_CD_NM", "INDIV_RAWMTRL_NM", "RAWMTRL_NM", "HIENG_LNTRT_DVS_NM",
            "PRODUCTION", "FRMLC_MTRQLT", "PRMS_DT", "ETC_RAWMTRL_NM"
    );

    private final Map<Character, Character> subscriptToDigit = Map.of(
            '₀', '0',
            '₁', '1',
            '₂', '2',
            '₃', '3',
            '₄', '4',
            '₅', '5',
            '₆', '6',
            '₇', '7',
            '₈', '8',
            '₉', '9'
    );

    public void importFromFile(String filePath) throws IOException {
        String allText = Files.readString(Path.of(filePath));

        // 멀티라인 모드로 item_id: 기준 split
        String[] blocks = allText.split("(?m)(?=^item_id:)");

        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                List<String> blockLines = Arrays.asList(block.split("\\r?\\n"));
                parseSingleBlock(blockLines);
            }
        }
    }

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
                .enterprise(filterContent(fields.get("BSSH_NM")))
                .productName(filterContent(fields.get("PRDLST_NM")))
                .registerDate(fields.get("LCNS_NO") !=null ? filterContent(fields.get("LCNS_NO")):"")
                .validTerm(fields.get("POG_DAYCNT")!= null ? filterContent(fields.get("POG_DAYCNT")):"")
                .form(fields.get("PRDT_SHAP_CD_NM") != null? filterContent(fields.get("PRDT_SHAP_CD_NM")):"")
                .dispos(fields.get("DISPOS") != null ? filterContent(fields.get("DISPOS")):"")
                .rawMaterial(fields.get("RAWMTRL_NM") != null? cleanBlank(fields.get("RAWMTRL_NM")):"")
                .indivMaterial(fields.get("INDIV_RAWMTRL_NM")!= null? cleanBlank(fields.get("INDIV_RAWMTRL_NM")):"")
                .build();

        supplement = healthSupplementRepository.save(supplement);

        //effect 저장
        String usage = fields.get("NTK_MTHD") != null? cleanContent(filterContent(fields.get("NTK_MTHD"))):"";
        String effect = fields.get("PRIMARY_FNCLTY") != null? cleanContent(filterContent(fields.get("PRIMARY_FNCLTY"))):"";
        String caution = fields.get("IFTKN_ATNT_MATR_CN") != null ? cleanContent(filterContent(fields.get("IFTKN_ATNT_MATR_CN"))):"";
        saveAllEffect(supplement, usage, effect, caution);

        //보관정보 저장
        String storage = fields.get("CSTDY_MTHD")!= null? cleanContent(filterContent(fields.get("CSTDY_MTHD"))):"";
        parseStorage(supplement, storage);

        // 성분 저장
        String ingr = fields.get("STDR_STND") != null ? cleanContentIng(fields.get("STDR_STND")):"";
        if(!ingr.isEmpty())
            parseIngredient(supplement, ingr);
    }

    private void parseStorage(HealthSupplement supplement, String storage) {
        /*
        - 온도조건: 실온에서 보관한다.
        - 용기: 밀봉용기
        - 습도: 정보 없음
        - 차광: 정보 없음
        */
        HealthSupplementStorageCondition temperature = new HealthSupplementStorageCondition();
        HealthSupplementStorageCondition fridge = new HealthSupplementStorageCondition();
        HealthSupplementStorageCondition humid = new HealthSupplementStorageCondition();
        HealthSupplementStorageCondition light = new HealthSupplementStorageCondition();


        temperature.setCategory(HealthSupplementStorageCondition.Category.TEMPERATURE);
        fridge.setCategory(HealthSupplementStorageCondition.Category.FRIDGE);
        light.setCategory(HealthSupplementStorageCondition.Category.LIGHT);
        humid.setCategory(HealthSupplementStorageCondition.Category.HUMID);
        temperature.setSupplement(supplement);
        fridge.setSupplement(supplement);
        light.setSupplement(supplement);
        humid.setSupplement(supplement);

        StringBuilder temText = new StringBuilder();
        StringBuilder friText = new StringBuilder();
        StringBuilder humText = new StringBuilder();
        StringBuilder ligText = new StringBuilder();

        String[] storageLines = storage.split("\n");
        for (String line : storageLines) {
            String s = line.trim();

            if(s.contains("냉장")) friText.append("냉장");
            if(s.contains("냉동")) {
                if(!friText.isEmpty()) friText.append(", ");
                friText.append("냉동");
            }

            if(s.contains("광")) ligText.append("직사광선x");

            if(s.contains("실온")) temText.append("실온보관");
            if(s.contains("고온") || s.contains("온장")) temText.append("고온x");

            if(s.contains("건조") || s.contains("건냉")) humText.append("건조한 곳");
            if(s.contains("습기") || s.contains("다습")) humText.append("습기가 적은 곳");
        }

        temperature.setValue(temText.toString());
        temperature.setActive(!temText.isEmpty());
        fridge.setValue(friText.toString());
        fridge.setActive(!friText.isEmpty());
        light.setValue(ligText.toString());
        light.setActive(!ligText.isEmpty());
        humid.setValue(humText.toString());
        humid.setActive(!humText.isEmpty());

        healthSupplementStorageConditionRepository.save(temperature);
        healthSupplementStorageConditionRepository.save(fridge);
        healthSupplementStorageConditionRepository.save(humid);
        healthSupplementStorageConditionRepository.save(light);
    }

    private void parseIngredient(HealthSupplement supplement, String ingr) {
        String[] ingrLines = ingr.split("\n");
        Pattern skipPattern = Pattern.compile(
                "\\s*\\d*\\s*\\(?(?:이하|음성)+"
        );
        Pattern namePattern = Pattern.compile(
                "[^함량규격:\\s][가-힣a-zA-Zγαβ\\d\\-+\\s,]*(\\(([-–])\\))?[가-힣a-zA-Z\\d-+(–),\\s*·/:γαβ]*(?=[①-⑳⑴-⑽]|[-•▪․;:：,(){}\\[\\]\\s]|표시량|으로서|로서)+"
        );
        Pattern unitPattern = Pattern.compile(
                "(μg|ug|mg|㎎|mcg|g[^\\da-zA-Z)\\[\\]}가-힣]|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)\\s*(?=\\s*[a-zA-Zα\\-]*)\\/*"
        );
        Pattern[] amountPatterns = {
                //패턴 1. 단위바로 앞에 값이 등장
                Pattern.compile(
                        "[^A-Za-z*/:%(){}\\[\\]가-힣\\s]+(?=\\s*(μg|ug|mg|㎎|mcg|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+\\s*[a-zA-Zɑα\\-]*\\s*\\/)+"
                ),
                //패턴 2. 억,조와 같이 한글이 혼용된 경우
                Pattern.compile(
                        "[^A-Za-z^(){}\\[\\]]*[\\d가-힣]+(?=\\s*(CFU|cfu)?\\s*\\/)"
                ),
                //패턴 3. 한글이 혼용된 경우 앞에 이미 수가 있는 경우
                Pattern.compile(
                        "[^A-Za-z가-힣]*[*×xX^,.\\s\\d]+(?=\\(\\s*[a-zA-Z]*\\s*[()\\d가-힣,.]+\\s*(CFU|cfu|cell)?\\s*\\/)"
                ),
                Pattern.compile(
                        "[^A-Za-z가-힣][\\s\\d,.]+(?=\\s*\\))"
                ),
                Pattern.compile(
                        "[^A-Za-z가-힣][\\s\\d,.*×xX^]+(μg|ug|mg|㎎|mcg|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+(?=\\s*\\()"
                ),
                Pattern.compile(
                        "[^A-Za-z가-힣)]+[\\s\\d,.*×xX^가-힣]+(?=μg|ug|mg|㎎|mcg|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+"
                ),
                Pattern.compile(
                        "[^A-Za-z가-힣)]+[\\s\\d,.*×xX^](?=\\s*\\()"
                )
        };
        for (String line : ingrLines) {
            line = Normalizer.normalize(line, Normalizer.Form.NFKC); // 전각 → 반각
            line = line.replaceAll("[\\p{Z}\\u200B]+", " "); // 모든 유니코드 공백을 일반 공백으로 통일
            Matcher matcher = skipPattern.matcher(line);
            if (matcher.find() ||
                    line.contains("대장균") || line.contains("붕해") || line.contains("성상") || line.contains("세균")
                    || line.contains("정제")|| line.contains("캡슐") || line.contains("불검출") || line.contains("확인")
                    || line.contains("액상") || line.contains("중금속") || line.contains("비율") || line.contains("시험방법")
                    || line.contains("납") || line.contains("카드뮴") || line.contains("비소") || line.contains("수은")
                    || line.contains("TAMC") || line.contains("TYMC") || line.contains("2액") || line.contains("연질")
                    || line.contains("일일섭취량") || line.contains("잔류용매") || line.contains("내용량") || line.contains("Clostridium")
                    || line.contains("Listeria") || line.contains("Vibrio") || line.contains("Coli")
                    || line.contains("coli") || line.contains("Salmo") || line.contains("Staphy") || line.isEmpty()) continue;

            String name = "";
            String amount = "";
            Double saveAmount = 0.0;
            String unit = "";

            //System.out.println(line);

            // 이름 매칭
            matcher = namePattern.matcher(line);
            if (matcher.find()) {
                name = matcher.group().trim();
                name = name
                        .replaceAll("함량","")
                        .replaceAll("으로서|로서","")
                        .replaceAll("의","")
                        .replaceAll("표시량","")
                        .replaceAll("성분","")
                        .replaceAll("최종제품|최종","")
                        .replaceAll("제품은|제품","")
                        .replaceAll("투입균|투입량","")
                        .replaceAll("(%)","")
                        .replaceAll("1회제공량당","")
                        .replaceAll("Acetate","")
                        .replaceAll("함유유지","")
                        .replaceAll("\\(\\)","")
                        .replaceAll("\\[*\\]*","")
                        .replaceAll("^\\s*(?:\\d+[,.）)]|\\d+-\\d+\\)|\\([^)]+\\)|[①-⑳⑴-⑽○]|[-•▪․\\[({]+|\\d+>|[가-힣]+\\.|\\d+-\\d+\\.)\\s*","")
                        .replaceAll("^\\.|ㆍ","")
                        .replaceAll("엘-","L-")
                        .replaceAll("기능또는지표-", "")
                        .replaceAll("Niacinarmide", "나이아신")
                        .replaceAll("Rosavin", "로사빈")
                        .replaceAll("VitB1", "비타민B1")
                        .replaceAll("VitB2", "비타민B2")
                        .replaceAll("VitE", "비타민E")
                        .replaceAll("[\\d.]*(g[^\\da-zA-zɑαβ]+|μg|ug|mg|㎎|mcg|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)\\s*(?=\\s*[αa-zA-z\\-]*)\\/*.*", "")
                        .replaceAll("(Ginsenoside|ginsenoside|진세노사드|\\d*진세노사이드|진노사이드)", "진세노사이드")
                        .replaceAll("N아세틸글루코사민", "N-아세틸글루코사민")
                        .replaceAll("Vitamin|\\d*비타민", "비타민");

                if(name.endsWith("-") || name.endsWith("*")
                        || name.endsWith("/") || name.endsWith(","))
                    name = name.substring(0, name.length()-1);

                name = findMatchedIngredient(name);
            }

            // 성분 섭취량 매칭
            for(int i=0; i<7; i++){
                if(line.contains("x") || line.contains("X") || line.contains("×") || line.contains("*")
                        || line.contains("^")){
                    Pattern pattern1 = Pattern.compile(
                            "[^A-Za-z*/:%(){}\\[\\]가-힣\\s]+[*×xX^,.\\d\\^\\sE]+(?=\\s*(μg|ug|mg|㎎|mcg|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+\\s*[a-zA-Zɑα\\-]*\\s*(\\/|\\)))+"
                    );
                    matcher = pattern1.matcher(line);
                    if(matcher.find()) {
                        //System.out.println("match");
                        amount = matcher.group().trim();
                        saveAmount =conAmount(amount);
                    }
                    break;
                }
                matcher = amountPatterns[i].matcher(line);
                if (matcher.find()) {
                    amount = matcher.group().trim();
                    if(i==0) {
                        if(amount.contains("~")){
                            Pattern wave = Pattern.compile(
                                    "(\\d+(?:\\.\\d+)?(?:μg|ug|mg|㎎|mcg|g|ml|CFU|cfu|cell|c\\.f\\.u|U|FU)?)\\s*[a-zA-Zɑα\\-]*\\s*~\\s*(\\d+(?:\\.\\d+)?(?:μg|ug|mg|㎎|mcg|g|ml|CFU|cfu|cells|c\\.f\\.u|U|FU)?)\\s*[a-zA-Zɑα\\-]*"
                            );
                            matcher = wave.matcher(line);
                            if(matcher.find()) amount = matcher.group().trim();
                        }
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }
                    else if(i==1) {
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        amount =amount.replaceAll("(프로바이오틱스\\s*(수|균)*:*\\s*)", "")
                                .replaceAll("이상", "")
                                .replaceAll("마리", "")
                                .replaceAll("\\s*","")
                                .replaceAll("[/:]", "");

                        saveAmount = conAmount(amount);
                        break;
                    }
                    else if(i==2) {
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }
                    else if(i==3) {
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }
                    else if(i==4){
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }
                    else if(i==5){
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }else{
                        if (!amount.matches(".*\\d.*")) continue; // 숫자 없으면 skip
                        saveAmount = conAmount(amount);
                        break;
                    }

                }
            }

            // 단위 매칭
            matcher = unitPattern.matcher(line);
            if (matcher.find()) {
                unit = matcher.group().trim();
                unit = unit.replaceAll("/","");
                unit = unit.replaceAll("\\s+","");
                unit = unit.replaceAll("㎎", "mg");
                unit = unit.replaceAll("(mcg|μg|ug)","μg");
                unit = unit.replaceAll("mL|ml" , "ml");
                unit = unit.replaceAll("(CFU|cfu|c\\.f\\.u)", "CFU");
            }
            if(saveAmount >= 100000000L) unit = "CFU";
            if(unit.isEmpty()) unit = "undefined";

            if(name.isEmpty() || name.equals("　") || saveAmount==0.0) {
                //System.out.println(name + " "+saveAmount+" "+unit);
                continue;
            }
            //System.out.println(name + " "+saveAmount+" "+unit);

            HealthIngredient healthIngredient = healthIngredientRepository.findByName(name).orElse(null);;
            HealthSupplementIngredient healthSupplementIngredient = new HealthSupplementIngredient();
            if(healthIngredient == null) {
                healthIngredient = new HealthIngredient();
                healthIngredient.setName(name);
                healthIngredient = healthIngredientRepository.save(healthIngredient);
            }
            healthSupplementIngredient.setIngredient(healthIngredient);
            healthSupplementIngredient.setSupplement(supplement);
            healthSupplementIngredient.setAmount(saveAmount);
            healthSupplementIngredient.setUnit(unit);
            ingredientRepository.save(healthSupplementIngredient);
        }
    }

    private Double conAmount(String amount) {
        if(amount.contains("조") || amount.contains("억") || amount.contains("만")) {
            return (double) koreanNumberParserService.parseKoreanNumber(amount);
        }

        amount = amount.replaceAll("[가-힣\\s/:;=]+","")
                .replaceAll("^\\.","");
        String[] parts = amount.split("[(){}\\[\\]]");
        if (parts.length > 0) {
            amount = parts[parts.length - 1]; // 마지막 토큰
        }

        amount = amount.replaceAll("(μg|ug|mg|mcg|㎎|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+","");
        amount = amount.replaceAll("(μg|ug|mg|mcg|㎎|g|ml|mL|CFU|cfu|cell|c\\.f\\.u|U|FU)+","");
        amount = normalizeNumberString(amount);
        amount = amount.replaceAll("[*×xX]+", "x");
        amount = amount.replaceAll("[μumglLCFcfU,]+","");
        amount = amount.replaceAll("(([aɑα])+ *\\-?(TE|te))","");
        if(amount.contains("x") || amount.contains("^")){
            return parseScientific(amount);
        }
        else if(amount.contains("~")){
            amount = amount.replaceAll("[^0-9.~]", "");
            String[] arr = amount.split("~");
            if (arr.length == 2) {
                double v1 = Double.parseDouble(arr[0]);
                double v2 = Double.parseDouble(arr[1]);
                return (v1 + v2) / 2;
            } else {
                throw new IllegalArgumentException("유효하지 않은 ~ 범위 입력: " + amount);
            }
        }
        return Double.valueOf(amount);
    }

    private String normalizeNumberString(String input) {
        long dotCount = input.chars().filter(ch -> ch == '.').count();

        if (dotCount >= 2) {
            // 천 단위 구분 기호로 보고 점 제거
            return input.replace(".", "");
        } else {
            // 그대로 유지 (실수 혹은 정수로 간주)
            return input;
        }
    }

    private String cleanBlank(String input) {
        Pattern pattern = Pattern.compile(
                "\\s+"
        );
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()) {
            input = matcher.replaceAll("");
            matcher = pattern.matcher(input);
        }
        return input;
    }


    private Double parseScientific(String input) {
        input = input.trim().replaceAll(",", ""); // 쉼표 제거
        //System.out.println("INPUT: " + input);

        // 1. 기본 파싱 시도 (예: 1.0E+08)
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException ignore) {}

        // 2. 잘못된 E 제거
        input = input.replaceAll("(?i)(\\d+(\\.\\d+)?)E(?![-+]?\\d+)", "$1");

        // 3. base x 10^exp 형태 (예: 1.0 x 10^11)
        Pattern sciPattern = Pattern.compile("(?i)(\\d+(\\.\\d+)?)\\s*[×xX*]?\\s*10\\^([\\d]+)");
        Matcher sciMatcher = sciPattern.matcher(input);
        if (sciMatcher.find()) {
            double base = Double.parseDouble(sciMatcher.group(1));
            int exponent = Integer.parseInt(sciMatcher.group(3));
            return base * Math.pow(10, exponent);
        }

        // 4. 단독 10^exp 형태 (예: 10^11)
        Pattern expOnlyPattern = Pattern.compile("(?i)10\\^([\\d]+)");
        Matcher expOnlyMatcher = expOnlyPattern.matcher(input);
        if (expOnlyMatcher.find()) {
            int exponent = Integer.parseInt(expOnlyMatcher.group(1));
            return Math.pow(10, exponent);
        }

        // 5. 단순 곱셈 (예: 5.0 x 100000000)
        Pattern mulPattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*[×xX*]\\s*(\\d+(\\.\\d+)?)");
        Matcher mulMatcher = mulPattern.matcher(input);
        if (mulMatcher.find()) {
            double left = Double.parseDouble(mulMatcher.group(1));
            double right = Double.parseDouble(mulMatcher.group(3));
            return left * right;
        }

        throw new IllegalArgumentException("패턴에 맞지 않습니다: " + input);
    }


    private void saveAllEffect(HealthSupplement supplement, String usage, String effect, String caution) {
        saveEffect(supplement, HealthSupplementEffect.Type.USAGE, usage);
        saveEffect(supplement, HealthSupplementEffect.Type.EFFECT, effect);
        saveEffect(supplement, HealthSupplementEffect.Type.CAUTION, caution);
    }

    private void saveEffect(HealthSupplement supplement, HealthSupplementEffect.Type type, String content){
        if (content != null && !content.isEmpty()) {
            HealthSupplementEffect se = new HealthSupplementEffect();
            se.setSupplement(supplement);
            se.setContent(content);
            se.setType(type);
            effectRepository.save(se);
        }
    }

    private String filterContent(String content) {
        // 개행 정리 + 앞뒤 공백 제거
        return content
                .replaceAll("(\\n\\s*){2,}", "\n")  // 2줄 이상 연속 개행 -> 1줄 개행
                .replaceAll("\t+", "\n")
                .replaceAll(" +", " ")              // 중복 공백 제거
                .trim();
    }

    private String normalizeSubscriptDigits(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            sb.append(subscriptToDigit.getOrDefault(c, c));
        }
        return sb.toString();
    }


    private String cleanContentIng(String content){
        Set<String> seen = new HashSet<>();
        List<String> result = new ArrayList<>();

        // 줄별로 자르기
        String[] lines = content.split("\\r?\\n");

        // 앞 번호/기호 정규식
        Pattern prefixPattern = Pattern.compile(
                "^　*\\s*(?:\\d+[.）)]|\\d+-\\d+\\)|\\([^)]+\\)|[①-⑳⑴-⒇○]|[-•▪․]+|\\d+>|[가-힣]+[.,)]|\\d+-\\d+\\.)\\s*"
        );

        for (int i=0; i<lines.length; i++) {
            String current = lines[i];
            while(i+1 < lines.length) {
                String next = lines[i + 1];
                //System.out.println(next);
                Pattern pattern = Pattern.compile("^　*\\s*(?:\\d+[,.）)]|\\d+-\\d+\\)|\\([^)]+\\)|[①-⑳⑴-⒇]|[-•▪․]+|\\d+>|[가-힣]+[.,)]|\\*|:)*\\s*(?:\\:|\\()*(원료|최종|합:|합 :|제품|표시량)+");
                Matcher matcher = pattern.matcher(next);
                if (matcher.find()) {
                    // 매치됨
                    //System.out.println("merge "+next);
                    current += " " + next.stripLeading(); // stripLeading()으로 앞 공백 제거 후 합치기
                    // skip 다음 줄
                    lines[i + 1] = ""; // 제거하거나, i++ 하고 continue
                    i++;
                }
                else break;
            }

            // 앞 번호/기호 반복 제거
            String cleaned = normalizeSubscriptDigits(current);
            Matcher matcher = prefixPattern.matcher(cleaned);
            while (matcher.find()) {
                //System.out.println("loop?" + " " + matcher.group());
                cleaned = cleaned.substring(matcher.end()).trim();
                matcher = prefixPattern.matcher(cleaned);
            }

            String remCleaned = cleaned.replaceAll(" +", "");
            if (!seen.contains(remCleaned)) {
                seen.add(remCleaned);
                result.add(cleaned);
            }
        }

        // 결과를 다시 하나의 문자열로 합치기
        return String.join("\n", result);
    }

    private String cleanContent(String content) {
        Set<String> seen = new HashSet<>();
        List<String> result = new ArrayList<>();

        // 줄별로 자르기
        String[] lines = content.split("\\r?\\n");

        // 앞 번호/기호 정규식
        Pattern prefixPattern = Pattern.compile(
                "^　*\\s*(?:\\d+[.）)]|\\d+-\\d+\\)|\\([^)]+\\)|[①-⑳⑴-⒇○]|[-•▪․]+|\\d+>|[가-힣]+[.,)]|\\d+-\\d+\\.)\\s*"
        );

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // 앞 번호/기호 반복 제거
            String cleaned = trimmed;
            Matcher matcher = prefixPattern.matcher(cleaned);
            while (matcher.find()) {
                cleaned = cleaned.substring(matcher.end()).trim();
                matcher = prefixPattern.matcher(cleaned);
            }

            String remCleaned = cleaned.replaceAll(" +", "");
            if (!seen.contains(remCleaned)) {
                seen.add(remCleaned);
                result.add("- " + cleaned);
            }
        }

        // 결과를 다시 하나의 문자열로 합치기
        return String.join("\n", result);
    }

    private String findMatchedIngredient(String input) {
        Pattern pattern = Pattern.compile(
                "[^가-힣a-zA-Z\\d-·ɑαβ\\s]+(수|함량|으로서|로서|으|으로써|(%)|의|제품|표시량|성분|투입균|최종|투입량|1회제공량당|기능|함유유지|\\[*\\]*|:)+"
        );
        Matcher matcher1 = pattern.matcher(input);
        while(matcher1.find()) {
            input = matcher1.replaceAll("");
            matcher1 = pattern.matcher(input);
        }
        input = input.replaceAll("(?<=\\d)(?=[a-zA-Z가-힣])", " "); // 숫자 뒤에 문자 있으면 구분
        input = input.replaceAll("\\b\\d+\\s+(?=[a-zA-Z가-힣])", ""); // 숫자 제거

        String text = cleanBlank(input);
        //System.out.println(text);
        text = text.toLowerCase();
        if(text.contains("프로바") || text.contains("프리바") || text.contains("유산균") || text.contains("비피더스")
                || text.contains("생균") || text.contains("probiotics") )  return "프로바이오틱스(유산균)";

        if(text.contains("홍삼") || ((text.contains("진세노") || text.contains("사이드")) && (text.contains("rg1")) && (text.contains("rb1")) && (text.contains("rg3"))))
            return "홍삼(진세노사이드 Rg1+Rb1+Rg3)";
        if( text.contains("인삼") || ((text.contains("진세노") || text.contains("사이드")) && (text.contains("rg1")) && (text.contains("rb1")))) {
            return "인삼(진세노사이드 Rg1+Rb1)";
        }
        if((text.contains("진세노") || text.contains("사이드")) && (text.contains("rg1")) && (text.contains("rg3"))) {
            return "진세노사이드 Rb1+Rg3";
        }
        if(text.contains("sakei")) return "sakei Probio65";
        if(text.contains("hydrangenol")) return "하이드란게놀(Hydrangenol)";
        if(text.contains("tanshinone")) return "Tanshinone IIA";
        if(text.contains("pgg")) return "PGG";
        if(text.contains("기타유래다당체")) return "기타유래다당체";
        if(text.contains("칼륨")) return "칼륨";
        if(text.contains("galactose") && text.contains("arabinose")) return "갈락토스(Galactose)+아라비노스(Arabinose)";
        if(text.contains("단신수") || text.contains("danshensu")) return "단신수(Danshensu)";
        if(text.contains("sod")) return "SOD 활성";
        if(text.contains("프락토")) return "프락토올리고당";
        if(text.contains("자일로")) return "자일로올리고당";
        if(text.contains("lactobacillus") || text.contains("락토바")) return "락토바실러스(Lactobacillus)";
        if(text.contains("이눌린") && text.contains("치커리")) return "이눌린/치커리 식이섬유";
        if(text.contains("치커리")) return "치커리추출물 식이섬유";
        if(text.contains("fustin")) return "옻나무추출물(Fustin)";
        if(text.contains("귀리식이섬유")) return "귀리식이섬유";
        if(text.contains("영양소") && text.contains("식이섬유")) return "영양소 식이섬유";
        if(text.contains("총") && text.contains("식이섬유")) return "총 식이섬유";
        if(text.contains("차전자피")) return "차전자피 식이섬유";
        if(text.contains("식이섬유")) return "식이섬유";
        if(text.contains("진세노사이드") && text.contains("re")) return "진세노사이드 Re";
        if(text.contains("진세노사이드") && text.contains("rg3") && text.contains("s")) return "진세노사이드 Rg3(S)";
        if(text.contains("진세노사이드") && text.contains("rg3") && text.contains("r")) return "진세노사이드 Rg3(R)";
        if(text.contains("발린") || text.contains("valine")) return "L-발린(L-Valine)";
        if(text.contains("isorhamnetin") || text.contains("이소람네틴")) return "이소람네틴(Isorhamnetin)";
        if(text.contains("카르니틴") || text.contains("carntine")) return "L-카르니틴(L-Carntine)";
        if(text.contains("열처리") || text.contains("배양균") || text.contains("breve")) return "B. breve IDCC 4401 열처리 배양균체";
        if (text.contains("망간")) return "망간";
        if (text.contains("크롬")) return "크롬";
        if (text.contains("요오드")) return "요오드";
        if (text.contains("티로신")) return "티로신";
        if(text.contains("구리")) return "구리";
        if(text.contains("actinidine")) return "악티니딘(Actinidine)";
        if(text.contains("비타민a")) return "비타민A";
        if(text.contains("비타민d")) return "비타민D";
        if(text.contains("비타민e")) return "비타민E";
        if(text.contains("비타민k1")) return "비타민K1";
        if(text.contains("비타민k2")) return "비타민K2";
        if(text.contains("비타민k")) return "비타민K";
        if(text.contains("마그네슘")) return "마그네슘";
        if(text.contains("조") && text.contains("단백질")) return "조단백질";
        if(text.contains("단백질")) return "단백질";
        if(text.contains("총") && text.contains("플라보노이드")) return "총 플라보노이드";
        if(text.contains("플라보노이드")) return "플라보노이드";
        if(text.contains("비타민b1") || text.contains("티아민") || text.contains("thiamine")) return "비타민B1(티아민(Thiamine Hydrochloride))";
        if(text.contains("비타민b12")) return "비타민B12";
        if(text.contains("하이페로") || text.contains("hyperoside")) return "하이페로시드(Hyperoside)";
        if(text.contains("코엔자임")) return "코엔자임Q10";
        if(text.contains("칼슘")) return "칼슘";
        if(text.contains("난소화성말토덱스트린")) return "난소화성말토덱스트린";
        if(text.contains("엽산")) return "비타민B9(엽산)";
        if(text.contains("인지질")) return "인지질";
        if(text.contains("prenylnaringenin")) return "8-프레닐나린게닌(8-Prenylnaringenin)";
        if(text.contains("acetylbritannilactone") || text.contains("아세틸브리타닐락톤")) return "1-O-아세틸브리타닐락톤(1-O-Acetylbritannilactone)";
        if(text.contains("디메틸에스쿨레틴") || text.contains("dimethylesculetin")) return "6,7-디메틸에스쿨레틴(6,7-Dimethylesculetin)";
        if(text.contains("아르기닌")) return "L-아르기닌";
        if(text.contains("deoxynojirimycin") || text.contains("데옥시노지리마이신")) return "1-데옥시노지리마이신(1-Deoxynojirimycin)";
        if(text.contains("공액리놀레산")) return "공액리놀레산";
        if((text.contains("다이드진") || text.contains("daidzin")) && (text.contains("제니스틴") || text.contains("genistin")) && (text.contains("글리시틴") || text.contains("glycitin"))) return "다이드진(Daidzin), 제니스틴(Genistin), 글리시틴(Glycitin)";
        if(text.contains("식물스테롤")) return "식물스테롤에스테르";
        if(text.contains("글루코사민") && text.contains("염산염")) return "글루코사민염산염";
        if(text.contains("글루코사민") && text.contains("황산염")) return "글루코사민황산염";
        if((text.contains("글리신")) || (text.contains("glycine")) && (text.contains("프롤린")) || (text.contains("proline"))) return "글리신(Glycine)+프롤린(Proline)";
        if(text.contains("총") && text.contains("엽록소")) return "총 엽록소(Total Chlorophyll)";
        if(text.contains("엽록소") || text.contains("chlorophyll")) return "엽록소(Chlorophyll)";
        if(text.contains("히알루론산")) return "히알루론산나트륨";
        if(text.contains("갈산") || text.contains("gallic")) return "갈산(Gallic acid)";
        if(text.contains("홍경천추출물") || text.contains("로사빈") || text.contains("rosavin")) return "홍경천추출물(로사빈(Rosavin)";
        if(text.contains("테아닌") || text.contains("theanine")) return "L-테아닌(L-theanine)";
        if(text.contains("뮤코다") || ( text.contains("단") && text.contains("백") && !text.contains("질")) ) return "뮤코다당·단백";
        if ((text.contains("총") && text.contains("플라보놀")) || (text.contains("glycosides") && text.contains("total") && text.contains("flavonol"))) return "총 플라보놀배당체 (Total flavonol glycosides)";
        if ((text.contains("총") && text.contains("폴리페놀")) || (text.contains("polyphenol") && text.contains("total"))) return "총 폴리페놀(Total polyphenol)";
        if ((text.contains("총") && text.contains("테르펜") && text.contains("락톤")) || (text.contains("lactones") && text.contains("terpene") && text.contains("total"))) return "총 테르펜 락톤(Total terpene lactones)";
        if(text.contains("compound")) return "Compound K";
        if(text.contains("베타카로틴")) return "베타-카로틴(β-caroten)";
        if(text.contains("진세노사이드") && text.contains("rg1")) return "진세노사이드 Rg1(Ginsenoside Rg1)";
        if ((text.contains("진세노사이드") || text.contains("ginsenoside")) && text.contains("rb1")) return "진세노사이드 Rb1(Ginsenoside Rb1)";
        if ((text.contains("커큐미노이드") && text.contains("총")) || (text.contains("total") && text.contains("curcuminoids"))) return "총 커큐미노이드(Total curcuminoids)";
        if ((text.contains("알파글루칸")) || (text.contains("α") && text.contains("glucan"))) return "알파글루칸(α-glucan)";
        if ((text.contains("베타글루칸")) || (text.contains("glucan") && text.contains("β"))) return "베타글루칸(β-glucan)";
        if ((text.contains("피노레시놀")) || (text.contains("pinoresinol") && text.contains("diglucoside"))) return "피노레시놀 디글루코사이드(Pinoresinol diglucoside)";
        if ((text.contains("히드록시엑디손")) || (text.contains("hydroxyecdysone") || text.contains("ecdysterone"))) return "20-히드록시엑디손(20-Hydroxyecdysone , Ecdysterone)";
        if ((text.contains("엘라그산")) || (text.contains("ellagic"))) return "엘라그산(Ellagic acid)";
        if ((text.contains("시아니딘")) || (text.contains("cyanidin"))) return "시아니딘(Cyanidin)";
        if ((text.contains("사이클로알리인")) || (text.contains("cycloalliin"))) return "사이클로알리인(Cycloalliin)";
        if ((text.contains("카테킨")) || (text.contains("catechin"))) return "카테킨(Catechin)";
        if ((text.contains("루테올린")) || (text.contains("luteolin"))) return "루테올린(Luteolin)";
        if ((text.contains("코로솔산")) || (text.contains("corosolicacid"))) return "코로솔산(Corosolic acid)(바나바잎추출물)";
        if ((text.contains("콘드로이친")) || (text.contains("chondroitin") && text.contains("sodium") && text.contains("sulfate"))) return "콘드로이친(Chondroitin sulfate sodium)";
        if ((text.contains("계피산")) || (text.contains("acid") && text.contains("cinnamic"))) return "계피산(Cinnamic acid)";
        if ((text.contains("산지사이드") && text.contains("메틸에스테르")) || (text.contains("methylester") && text.contains("shanzhiside"))) return "산지사이드 메틸에스테르(Shanzhiside methylester)";
        if ((text.contains("노다케닌")) || (text.contains("nodakenin"))) return "노다케닌(Nodakenin)";
        if ((text.contains("클로로겐산")) || (text.contains("acid") && text.contains("chlorogenic"))) return "클로로겐산(Chlorogenic acid)";
        if ((text.contains("엘레우테로사이드")) || (text.contains("e") && text.contains("eleutheroside"))) return "엘레우테로사이드 E(Eleutheroside E)";
        if ((text.contains("스타키오스")) || (text.contains("stachyose"))) return "스타키오스(Stachyose)";
        if ((text.contains("구연산")) || (text.contains("acid") && text.contains("citric"))) return "구연산(Citric acid)";
        if ((text.contains("폴리감마")) || text.contains("pga")) return "폴리감마글루탐산(γ-PGA)";
        if ((text.contains("비타민c")) || (text.contains("acerola") && text.contains("cherry") && text.contains("extract")) || (text.contains("acid") && text.contains("ascorbic")) ) return "비타민C(AcerolaCherryExtract, Ascorbic Acid)";
        if ((text.contains("코디세핀")) || (text.contains("cordycepin"))) return "코디세핀(Cordycepin)";
        if ((text.contains("크레아틴")) || (text.contains("creatine"))) return "크레아틴(Creatine monohydrate)";
        if ((text.contains("판토텐산")) || (text.contains("pantothenate"))) return "비타민B5(판토텐산(Calcium Pantothenate))";
        if ( text.contains("비타민e") || text.contains("토코페롤") || text.contains("tocopherol")) return "비타민E(D-α-토코페롤(D-α-Tocopherol))";
        if ((text.contains("피니톨")) || (text.contains("pinitol") && text.contains("d"))) return "D-피니톨(D-pinitol)";
        if ((text.contains("커큐민")) || (text.contains("curcumin"))) return "커큐민(Curcumin)";
        if ((text.contains("알파리놀렌산")) || (text.contains("linolenic") && text.contains("dha") && text.contains("acid") && text.contains("α") && text.contains("dpa") && text.contains("epa"))) return "초록입홍합추출물(EPA+DHA+DPA+알파리놀렌산(α-linolenic acid))";
        if ((text.contains("dha") && text.contains("epa")) || text.contains("오메가")) return "오메가 3(EPA+DHA)";
        if ((text.contains("리놀렌산")) || (text.contains("acid") && text.contains("linolenic"))) return "리놀렌산(Linolenic acid)";
        if ((text.contains("피로갈롤")) || (text.contains("polyphenol") && text.contains("derivatives") && text.contains("pyrogallol"))) return "피로갈롤(Pyrogallol, Polyphenol derivatives)";
        if ((text.contains("다이드진")) || (text.contains("daidzin"))) return "다이드진(Daidzin)";
        if ((text.contains("제니스틴")) || (text.contains("genistin"))) return "제니스틴(Genistin)";
        if ((text.contains("글리시틴")) || (text.contains("glycitin"))) return "글리시틴(Glycitin)";
        if ((text.contains("디메틸설폰") || text.contains("엠에스엠")) || (text.contains("sulfone") && text.contains("dimethyl")) || text.contains("msm")) return "디메틸설폰(Dimethyl Sulfone, MSM)";
        if ((text.contains("디엑콜")) || (text.contains("dieckol"))) return "디엑콜(Dieckol)";
        if ((text.contains("글라브리딘")) || (text.contains("glabridin"))) return "글라브리딘(Glabridin)";
        if ((text.contains("가스트로딘")) || (text.contains("gastrodin"))) return "가스트로딘(Gastrodin)";
        if ( (text.contains("cit")  && text.contains("hydroxy")) || text.contains("hca")) return "가르시니아캄보지아(Hydroxycitric Acid(HCA))";
        if ((text.contains("타우린")) || (text.contains("taurine"))) return "타우린(Taurine)";
        if ((text.contains("감마") && text.contains("아미노부틸산")) || ((text.contains("aminobutyric") && text.contains("γ") && text.contains("acid")) || text.contains("gaba"))) return "감마-아미노부틸산(γ-aminobutyric acid, GABA)";
        if ((text.contains("글루코실세라미드")) || (text.contains("glucosylceramide"))) return "글루코실세라미드(Glucosylceramide)";
        if ((text.contains("프롤린")) || (text.contains("proline"))) return "프롤린(Proline)";
        if ((text.contains("글리신")) || (text.contains("glycine"))) return "글리신(Glycine)";
        if ((text.contains("카페인")) || (text.contains("caffeine"))) return "카페인(Caffeine)";
        if ((text.contains("헤스페리딘")) || (text.contains("hesperidin"))) return "헤스페리딘(Hesperidin)";
        if ((text.contains("류실글리신")) || (text.contains("leucylglycine") && text.contains("l"))) return "L-류실글리신(L-Leucylglycine)";
        if ((text.contains("리신")) || (text.contains("lysine") && text.contains("l"))) return "L-리신(L-Lysine)";
        if ((text.contains("플라보놀배당체")) || (text.contains("flavonol") && text.contains("glycosides"))) return "플라보놀배당체 (flavonol glycosides)";
        if ((text.contains("로이신")) || (text.contains("leucine") && text.contains("l"))) return "L-로이신(L-Leucine)";
        if ((text.contains("라우르산") || text.contains("로르산")) || (text.contains("acid") && text.contains("lauric"))) return "라우르산(Lauric acid)";
        if ((text.contains("나이아신")) || text.contains("niacin")) return "비타민B3(나이아신(Niacin,Niacinarmide))";
        if ((text.contains("나린진")) || (text.contains("naringin")) || text.contains("natto") || text.contains("나토")) return "나토균배양분말(나린진(Naringin))";
        if ((text.contains("쿠마르산")) || (text.contains("coumaric") || text.contains("acid"))) return "p-쿠마르산(p-Coumaric acid)";
        if (text.contains("비타민b6") || (text.contains("피리독신")) || (text.contains("pyridoxine") && text.contains("hydrochloride"))) return "비타민B6(피리독신(Pyridoxine Hydrochloride))";
        if ((text.contains("퀘르세틴")) || (text.contains("quercetin"))) return "퀘르세틴(Quercetin)";
        if ((text.contains("퀸산")) || (text.contains("acid") && text.contains("quinic"))) return "퀸산(Quinic acid)";
        if ((text.contains("퀴스퀴알산")) || (text.contains("acid") && text.contains("quisqualic"))) return "퀴스퀴알산(Quisqualic acid)";
        if ((text.contains("라폰티신")) || (text.contains("rhaponticin"))) return "라폰티신(Rhaponticin)";
        if (text.contains("비타민b2") || (text.contains("리보플라빈")) || (text.contains("riboflavin"))) return "비타민B2(리보플라빈(Riboflavin))";
        if ((text.contains("로즈마리산")) || (text.contains("acid") && text.contains("rosmarinic"))) return "로즈마리산(Rosmarinic acid)";
        if ((text.contains("살비아놀릭산")) || (text.contains("salvianolic") && text.contains("acid") && text.contains("b"))) return "살비아놀릭산 B(Salvianolic acid B)";
        if ((text.contains("세콕시로가닌")) || (text.contains("secoxyloganin"))) return "세콕시로가닌(Secoxyloganin)";
        if (text.contains("밀크씨슬") || text.contains("실리마린") || text.contains("silymarin")) return "밀크씨슬(실리마린)(Silymarin)";
        if ((text.contains("밀리아신")) || (text.contains("miliacin"))) return "밀리아신(Miliacin)";
        if ((text.contains("소포리코사이드")) || (text.contains("sophoricoside"))) return "소포리코사이드(Sophoricoside)";
        if ((text.contains("스핑고마이엘린")) || (text.contains("sphingomyelin"))) return "스핑고마이엘린(Sphingomyelin)";
        if ((text.contains("틸리아닌")) || (text.contains("tilianin"))) return "틸리아닌(Tilianin)";
        if ((text.contains("피에오니플로린")) || (text.contains("paeoniflorin"))) return "피에오니플로린(Paeoniflorin)";
        if ((text.contains("아세틸글루코사민") || text.contains("엔에이지")) || (text.contains("n") && text.contains("acetylglucosamine")) || text.contains("nag") ) return "N-아세틸글루코사민(NAG, N-Acetylglucosamine)";
        if (text.contains("hamabiwalactone") && text.contains("b")) return "Hamabiwalactone B";
        if ((text.contains("지페노사이드")) || (text.contains("gypenoside") && text.contains("l"))) return "지페노사이드 L(Gypenoside L)";
        if (text.contains("1") && text.contains("acetylbritannilactone") && text.contains("o")) return "1-O-acetylbritannilactone";
        if ((text.contains("3") && text.contains("palmitoyl") && text.contains("acetyl") && text.contains("2") && text.contains("glycerol") && text.contains("1") && text.contains("linoleoyl") && text.contains("rac")) || text.contains("plag")) return "1-palmitoyl-2-linoleoyl-3-acetyl-rac-glycerol (PLAG)";
        if ((text.contains("테트라코사놀")) || (text.contains("1") && text.contains("tetracosanol"))) return "1-테트라코사놀(1-tetracosanol)";
        if ( text.contains("비즈왁스알코올") || (text.contains("heptacosanol") && text.contains("nonaco") && text.contains("1") && text.contains("tetracosanol") && text.contains("otacosanol") && text.contains("hexacosanol"))) return "1-tetracosanol,1-hexacosanol,1-heptacosanol,1-otacosanol,1-nonaco";
        if (text.contains("heptacosanol") && text.contains("octacosanol") && text.contains("1") && text.contains("dotriacontanol") && text.contains("hexacosanol") && text.contains("triacontanol") && text.contains("nonacosanol")) return "1-hexacosanol,1-heptacosanol,1-octacosanol,1-nonacosanol,1-triacontanol,1-dotriacontanol";
        if ((text.contains("진저다이온") && text.contains("디하이드로")) || (text.contains("dehydro") && text.contains("gingerdione") && text.contains("1") && text.contains("6"))) return "1-디하이드로-6-진저다이온(1-Dehydro-6-gingerdione)";
        if ((text.contains("데쿠르신")) || (text.contains("decursin"))) return "데쿠르신(Decursin)";
        if ((text.contains("테르피놀렌")) || (text.contains("terpinolene"))) return "테르피놀렌(Terpinolene)";
        if ((text.contains("카렌")) || (text.contains("3") && text.contains("carene"))) return "3-카렌(3-Carene)";
        if ((text.contains("리모넨")) || (text.contains("limonene"))) return "리모넨(Limonene)";
        if ((text.contains("푸에라린")) || (text.contains("puerarin"))) return "푸에라린(Puerarin)";
        if ((text.contains("포름오노네틴")) || (text.contains("formononetin"))) return "포름오노네틴(Formononetin)";
        if ((text.contains("스코폴레틴")) || (text.contains("scopoletin"))) return "스코폴레틴(Scopoletin)";
        if ((text.contains("아피제닌")) || (text.contains("apigenin"))) return "아피제닌(Apigenin)";
        if ((text.contains("아스트라갈린")) || (text.contains("astragalin"))) return "아스트라갈린(Astragalin)";
        if ((text.contains("베타인")) || (text.contains("betaine"))) return "베타인(Betaine)";
        if ((text.contains("이소로이신") && text.contains("하이드록시")) || (text.contains("hydroxy") && text.contains("l") && text.contains("4") && text.contains("isoleucine"))) return "4-하이드록시-L-이소로이신(4-Hydroxy-L-isoleucine)";
        if ((text.contains("루테올린") && text.contains("글루코사이드")) || (text.contains("o") && text.contains("luteolin") && text.contains("7") && text.contains("diglucuronide"))) return "루테올린-7-O-글루코사이드(Luteolin-7-O-diglucuronide)";
        if (text.contains("acid") && text.contains("trimethoxycinnamic")) return "Trimethoxycinnamic acid";
        if ((text.contains("베라트릭산")) || (text.contains("acid") && text.contains("veratric"))) return "베라트릭산(Veratric acid)";
        if ((text.contains("버바스코사이드")) || (text.contains("verbascoside"))) return "버바스코사이드(Verbascoside)";
        if ((text.contains("비텍신")) || (text.contains("vitexin"))) return "비텍신(Vitexin)";
        if ((text.contains("위타노사이드")) || (text.contains("iv") && text.contains("withanoside"))) return "위타노사이드 IV(Withanoside IV)";
        if ((text.contains("산화아연")) || (text.contains("zinc") && text.contains("oxide"))) return "산화아연(Zinc Oxide)";
        if (text.contains("아연")) return "아연(Zinc)";
        if(text.contains("기타유래폴리페놀")) return "기타유래폴리페놀";
        if(text.contains("총") && text.contains("지방족") && text.contains("알코올")) return "총 지방족 알코올";
        if (text.contains("프로토카테츄산") || (text.contains("protocatechuic") && text.contains("acid"))) return "프로토카테츄산(Protocatechuic acid)";
        if ((text.contains("가르시니아캄보지아")) || (text.contains("hca"))) return "가르시니아캄보지아(HCA)";
        if ((text.contains("갈랑긴")) || (text.contains("galangin"))) return "갈랑긴(Galangin)";
        if ((text.contains("감마") && text.contains("오리자놀")) || (text.contains("oryzanol") && text.contains("γ"))) return "감마-오리자놀(γ-Oryzanol)";
        if ((text.contains("글리시리진산")) || (text.contains("acid") && text.contains("glycyrrhizic"))) return "글리시리진산(Glycyrrhizic acid)";
        if ((text.contains("히드록시프롤린") && text.contains("글리신") && text.contains("프롤린")) || (text.contains("hyp") && text.contains("pro") && text.contains("gly")) || text.contains("gph")) return "글리신-프롤린-히드록시프롤린(Gly-Pro-Hyp(GPH))";
        if ((text.contains("깅콜릭산")) || (text.contains("acid") && text.contains("ginkgolic"))) return "깅콜릭산(ginkgolic acid)";
        if ((text.contains("카테킨")) || (text.contains("catechin"))) return "카테킨(Catechin)";
        if ((text.contains("다물린a")) || (text.contains("damulina"))) return "다물린A(DamulinA)";
        if ((text.contains("라피노스")) || (text.contains("raffinose"))) return "라피노스(Raffinose)";
        if ((text.contains("락추로스")) || (text.contains("lactulose"))) return "락추로스(Lactulose)";
        if ((text.contains("락토페린")) || (text.contains("lactoferrin"))) return "락토페린(Lactoferrin)";
        if ((text.contains("레갈로사이드")) || (text.contains("regaloside") && text.contains("a"))) return "레갈로사이드 A(Regaloside A)";
        if ((text.contains("레오누린")) || (text.contains("leonurine"))) return "레오누린(Leonurine)";
        if ((text.contains("루틴")) || (text.contains("rutin"))) return "루틴(Rutin)";
        if ((text.contains("리그스틸라이드")) || (text.contains("ligustilide"))) return "리그스틸라이드(Ligustilide)";
        if ((text.contains("리나린")) || (text.contains("linarin"))) return "리나린(Linarin)";
        if ((text.contains("리소스펌산")) || (text.contains("lithospermic") && text.contains("acid"))) return "리소스펌산(Lithospermic acid)";
        if (text.contains("루테인") || text.contains("마리골드") || text.contains("lutein")) return "마리골드 꽃 추출물(루테인(Lutein))";
        if ((text.contains("만노스")) || (text.contains("mannose"))) return "만노스(mannose)";
        if ((text.contains("모나콜린")) || (text.contains("monacholine") && text.contains("k"))) return "모나콜린-K (Monacholine-K)";
        if ((text.contains("몰리브덴")) || (text.contains("molybdenum"))) return "몰리브덴(Molybdenum, Mo)";
        if (text.contains("안트라퀴논계화합물") || text.contains("무수바바로인")) return "안트라퀴논계화합물(무수바바로인)";
        if ((text.contains("미리세틴")) || (text.contains("myricetin"))) return "미리세틴(Myricetin)";
        if ((text.contains("쉬잔드린")) || (text.contains("schizandrin"))) return "쉬잔드린(Schizandrin)";
        if ((text.contains("미리스트올레산")) || (text.contains("myristoleic") && text.contains("acid"))) return "미리스트올레산(Myristoleic acid)";
        if ((text.contains("미퀠리아닌")) || (text.contains("miquelianin"))) return "미퀠리아닌(Miquelianin)";
        if ((text.contains("밀크씨슬") || text.contains("실리마린")) || (text.contains("silymarin"))) return "밀크씨슬(실리마린(Silymarin))";
        if ((text.contains("바닐릭산")) || (text.contains("acid") && text.contains("vanillic"))) return "바닐릭산(Vanillic acid)";
        if (text.contains("pro") && text.contains("val") && text.contains("gly") && text.contains("ala")) return "Gly-Ala-Val-Gly-Pro-Ala";
        if (text.contains("pro") && text.contains("gly") && text.contains("dipeptide")) return "Gly-Pro-dipeptide";
        if (text.contains("hyp") && text.contains("ala") && text.contains("pro") && text.contains("val") && text.contains("gly")) return "Val-Gly-Pro-Hyp-Gly-Pro-Ala-Gly";
        if (text.contains("pro") && text.contains("val") && text.contains("gly") && text.contains("ser")) return "Gly-Pro-Val-Gly-Pro-Ser";
        if (text.contains("prolyl") && text.contains("l") && text.contains("valylglycyl") && text.contains("glycine") && text.contains("vgpg")) return "Glycine, L-Valylglycyl-L-prolyl-(VGPG)";
        if (text.contains("gln") && text.contains("ile") && text.contains("lys") && text.contains("asp") && text.contains("leu")) return "Leu-Asp-Ile-Gln-Lys";
        if ((text.contains("바릴티로신")) || (text.contains("val") && text.contains("tyr"))) return "바릴티로신(Val-Tyr)";
        if ((text.contains("바이칼린")) || (text.contains("baicalin"))) return "바이칼린(Baicalin)";
        if ((text.contains("베타") && text.contains("시토스테롤")) || (text.contains("β") && text.contains("sitosterol"))) return "베타-시토스테롤(β-sitosterol)";
        if ((text.contains("베타인")) || (text.contains("betaine"))) return "베타인(Betaine)";
        if (text.contains("hmb") || (text.contains("hydroxy") && text.contains("methylbutyric"))) return "HMB(β-Hydroxy β-methylbutyric acid)";
        if ((text.contains("벤질헥사데칸아미드")) || (text.contains("benzylhexadecanamide"))) return  "벤질헥사데칸아미드(Benzylhexadecanamide)";
        if ((text.contains("비오틴")) || (text.contains("biotin"))) return "비타민B7(비오틴(Biotin))";
        if ((text.contains("사우치논")) || (text.contains("sauchinone"))) return "사우치논(Sauchinone)";
        if ((text.contains("사포나린")) || (text.contains("saponarin"))) return "사포나린(Saponarin)";
        if ((text.contains("사포닌")) || (text.contains("saponin"))) return "사포닌(Saponin)";
        if ((text.contains("세린")) || (text.contains("serine"))) return "세린(Serine)";
        if ((text.contains("세틸미리스톨레이트")) || (text.contains("myristoleate") && text.contains("cetyl"))) return "세틸미리스톨레이트(Cetyl Myristoleate)";
        if ((text.contains("셀렌") || text.contains("셀레늄")) || (text.contains("selenium"))) return "셀레늄(셀렌, Selenium)";
        if ((text.contains("스쿠알렌")) || (text.contains("squalene"))) return "스쿠알렌(Squalene)";
        if ((text.contains("스피루리나")) || (text.contains("spirulina"))) return "스피루리나(Spirulina)";
        if ((text.contains("시알산")) || (text.contains("acid") && text.contains("sialic"))) return "시알산(Sialic acid)";
        if ((text.contains("라우르산") || text.contains("쏘팔메토열매추출물")) || (text.contains("acid") && text.contains("lauric"))) return "쏘팔메토열매추출물(라우르산(Lauric acid))";
        if ((text.contains("디엑콜") || text.contains("씨폴리놀감태주정추출물")) || (text.contains("dieckol"))) return "씨폴리놀감태주정추출물(디엑콜(Dieckol))";
        if ((text.contains("아데노신")) || (text.contains("adenosine"))) return "아데노신(Adenosine)";
        if ((text.contains("아스타잔틴")) || (text.contains("astaxanthin"))) return "아스타잔틴(Astaxanthin)";
        if ((text.contains("아시아티코사이드")) || (text.contains("asiaticoside"))) return "아시아티코사이드(Asiaticoside)";
        if ((text.contains("안드로그라폴라이드")) || (text.contains("andrographolide"))) return "안드로그라폴라이드(Andrographolide)";
        if ((text.contains("안토시아노사이드")) || (text.contains("anthocyanidin"))) return "안토시아노사이드(Anthocyanidin)";
        if ((text.contains("알라닌")) || (text.contains("alanine"))) return "알라닌(Alanine)";
        if ((text.contains("알란토인")) || (text.contains("allantoin"))) return "알란토인(Allantoin)";
        if ((text.contains("알리인")) || (text.contains("alliin"))) return "알리인(Alliin)";
        if (text.contains("lycopene") || text.contains("라이코펜")) return "라이코펜((all-trans)-lycopene)";
        if ((text.contains("알콕시글리세롤")) || (text.contains("alkoxyglycerol"))) return "알콕시글리세롤(Alkoxyglycerol)";
        if ((text.contains("알파") && text.contains("망고스틴")) || (text.contains("mangosteen"))) return "알파-망고스틴(α-Mangosteen)";
        if ((text.contains("카제인") && text.contains("알파에스1")) || (text.contains("casein"))) return "알파에스1-카제인(αS1-casein)";
        if (text.contains("앤고로사이드c")) return "앤고로사이드C";
        if ((text.contains("에이코사펜타엔산")) || (text.contains("epa"))) return "에이코사펜타엔산(EPA)";
        if (text.contains("에피갈로카테킨갈레이트") || text.contains("egcg") || text.contains("epigalo")) return "에피갈로카테킨갈레이트((-)-epigallocatechin gallate, EGCG)";
        if ((text.contains("오노닌")) || (text.contains("ononin"))) return "오노닌(Ononin)";
        if ((text.contains("오리엔틴")) || (text.contains("orientin"))) return "오리엔틴(Orientin)";
        if ((text.contains("오메가")) || (text.contains("dha") && text.contains("epa"))) return "오메가 3(EPA+DHA)";
        if ((text.contains("옥시레스베라트롤")) || (text.contains("oxyresveratrol"))) return "옥시레스베라트롤(Oxyresveratrol)";
        if ((text.contains("옥타코사놀")) || (text.contains("octacosanol"))) return "옥타코사놀(Octacosanol)";
        if ((text.contains("우라실")) || (text.contains("uracil"))) return "우라실(Uracil)";
        if ((text.contains("유리딘")) || (text.contains("uridine"))) return "유리딘(Uridine)";
        if ((text.contains("이소비텍신")) || (text.contains("isovitexin"))) return "이소비텍신(Isovitexin)";
        if ((text.contains("이소퀘르시트린")) || (text.contains("isoquercitrin"))) return "이소퀘르시트린(Isoquercitrin)";
        if (text.contains("이소플라본") || text.contains("비배당체")) return "대두이소플라본(비배당체)";
        if ((text.contains("포스파티딜")) || (text.contains("phosphatidylcholine"))) return "포스파티딜콜린(Phosphatidylcholine)";
        if ((text.contains("제니포시딕산")) || (text.contains("geniposidic") && text.contains("acid"))) return "제니포시딕산(Geniposidic acid)";
        if ((text.contains("제이인산칼슘")) || (text.contains("calcium") && text.contains("phosphate"))) return "제이인산칼슘(Calcium Phosphate)";
        if ((text.contains("지아잔틴")) || (text.contains("zeaxanthin"))) return "지아잔틴(Zeaxanthin)";
        if (text.contains("철") || text.contains("철분")) return "철";
        if (text.contains("알로에겔") || (text.contains("총") && text.contains("다당체"))) return "알로에겔(총 다당체)";
        if (text.contains("코엔자임q10")) return "코엔자임Q10";
        if ((text.contains("쿠메스테롤")) || (text.contains("coumestrol"))) return "쿠메스테롤(Coumestrol)";
        if ((text.contains("클로렐라")) || (text.contains("chlorella"))) return "클로렐라(Chlorella)";
        if ((text.contains("텍토크리신")) || (text.contains("tectochrysin"))) return "텍토크리신(Tectochrysin)";
        if ((text.contains("티모퀴논")) || (text.contains("thymoquinone"))) return "티모퀴논(Thymoquinone)";
        if ((text.contains("판두라틴")) || (text.contains("a") && text.contains("panduratin"))) return "판두라틴 A(Panduratin A)";
        if ((text.contains("팔밋올레산")) || (text.contains("acid") && text.contains("palmitic"))) return "팔밋올레산(Palmitic acid)";
        if ((text.contains("포스콜린") || text.contains("포스콜리")) || (text.contains("forskolin"))) return "포스콜린(Forskolin)";
        if ((text.contains("폴리덱스트로스")) || (text.contains("polydextrose"))) return "폴리덱스트로스(Polydextrose)";
        if ((text.contains("푸닉산")) || (text.contains("acid") && text.contains("punicic"))) return "푸닉산(Punicic acid)";
        if ((text.contains("플로리진")) || (text.contains("phloridzin"))) return "플로리진(Phloridzin)";
        if (text.contains("키토산")) return "키토산";
        if(text.contains("니코틴산아미드")) return "비타민B3(니코틴산아미드(Nicotinamide))";
        if (text.contains("키토올리고당")) return "키토올리고당";
        if(text.contains("아밀라아제")) return "α-아밀라아제";
        if (text.contains("프로테아제")) return "프로테아제";
        if (text.contains("피브린") || text.contains("피브로")) return "피브린용해활성";
        if (text.contains("하이포잔틴")) return "하이포잔틴";
        if ((text.contains("acetyl") || text.contains("akba")) && (text.contains("keto") || text.contains("kba"))) return "보스웰리아추출물(3-Acetyl-11-keto-β-boswellic acid(AKBA)와 11-keto-β-boswellic acid(KBA))";
        if ((text.contains("acetyl") || text.contains("akba"))) return "3-Acetyl-11-keto-β-boswellic acid(AKBA)";
        if ((text.contains("keto") || text.contains("kba"))) return "11-keto-β-boswellic acid(KBA)";
        if(text.contains("글루코사민")) return "글루코사민";
        if(text.contains("tmca")) return "TMCA";
        if(text.contains("자일리톨")) return "자일리톨";
        if(text.contains("oleuropein")) return "올레유러페인(Oleuropein)";
        if(text.contains("게르마늄")) return "게르마늄";
        if(text.contains("주석산")) return "L-주석산";
        return input;
    }
}
