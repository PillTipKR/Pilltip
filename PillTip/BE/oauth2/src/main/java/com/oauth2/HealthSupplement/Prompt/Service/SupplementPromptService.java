package com.oauth2.HealthSupplement.Prompt.Service;

import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.Prompt.Dto.*;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementDetail;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementRequestInfoDto;
import com.oauth2.HealthSupplement.Prompt.Dto.SupplementPromptRequestDto;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementSummaryResponse;
import com.oauth2.User.TakingSupplement.Service.TakingSupplementService;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Service.UserSensitiveInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplementPromptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String API_URL;

    @Value("${openai.model}")
    private String model;

    private final TakingPillService takingPillService;
    private final TakingSupplementService takingSupplementService;
    private final UserSensitiveInfoService userSensitiveInfoService;


    private SupplementPromptRequestDto buildPromptRequestDto(User user, SupplementDetail detail) {
        List<String> medicationNames = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationName)
                .toList();
        List<String> supplementNames = takingSupplementService.getTakingSupplementSummary(user).getTakingSupplements().stream()
                .map(TakingSupplementSummaryResponse.TakingSupplementSummary::getSupplementName)
                .toList();

        List<DurTagDto> trueTags = detail.durTags().stream()
                .filter(DurTagDto::isTrue)
                .toList();

        UserSensitiveInfoDto userSensitiveInfo = userSensitiveInfoService.getSensitiveInfo(user);
        String chronicDiseaseInfo = "";
        String allergyInfo = "";
        if(userSensitiveInfo != null) {
            chronicDiseaseInfo = userSensitiveInfo.getChronicDiseaseInfo().toString();
            allergyInfo = userSensitiveInfo.getAllergyInfo().toString();
        }
        return new SupplementPromptRequestDto(
                trueTags,
                user.getNickname(),
                user.getUserProfile().getAge(),
                user.getUserProfile().getGender().name(),
                user.getUserProfile().isPregnant(),
                chronicDiseaseInfo,
                allergyInfo,
                medicationNames,
                supplementNames,
                new SupplementRequestInfoDto(
                        detail.name(),
                        detail.effect(),
                        detail.usage(),
                        detail.caution()
                )
        );
    }

    public String getAsk(User user, SupplementDetail detail) {
        String prompt = buildPrompt(buildPromptRequestDto(user, detail));
        return askGPT(prompt);
    }

    public DurResponse askDur(DurAnalysisResponse durAnalysisResponse){
        String prompt = buildCombinedDurPrompt(durAnalysisResponse);
        String gptResponse = askGPT(prompt); // OpenAI 응답 전체 텍스트
        DurExplanationResult result = parseCombinedResponse(gptResponse);

        String drugExplanation = result.drugA();
        String supplementExplanation = result.drugB();
        String interactExplanation = result.interact();

        return new DurResponse(
                durAnalysisResponse.durA().drugName(),
                durAnalysisResponse.durB().drugName(),
                drugExplanation,
                supplementExplanation,
                interactExplanation,
                !durAnalysisResponse.durA().durtags().isEmpty(),
                !durAnalysisResponse.durB().durtags().isEmpty(),
                !durAnalysisResponse.interact().durtags().isEmpty()
        );
    }

    private String askGPT(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        // 메시지 구성
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        GPTRequest request = new GPTRequest(
                model,
                Collections.singletonList(userMessage),
                0.3,
                1000

        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<GPTRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GPTResponse> response = restTemplate.postForEntity(API_URL, entity, GPTResponse.class);

        assert response.getBody() != null;
        return response.getBody().getChoices().get(0).getMessage().getContent();
    }

    private String buildCombinedDurPrompt(DurAnalysisResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append("아래는 한 사용자가 복용하려는 약-건강기능식품간 병용 조합에 대한 DUR 정보예요.\n\n");

        // 약물 A
        sb.append("약/건강기능식품:\n");
        sb.append("- name: ").append(response.durA().drugName()).append("\n");
        sb.append("- durtags: ");
        appendDurTagsExpanded(sb, response.durA().durtags());
        sb.append("- isTakingOtherDrugs: ").append(response.userTaken()).append("\n\n");

        // 약물 B
        sb.append("약/건강기능식품:\n");
        sb.append("- name: ").append(response.durB().drugName()).append("\n");
        sb.append("- durtags: ");
        appendDurTagsExpanded(sb, response.durB().durtags());
        sb.append("- isTakingOtherDrugs: ").append(response.userTaken()).append("\n\n");

        // 병용 DUR
        sb.append("병용 DUR:\n");
        sb.append("- 조합: ").append(response.durA().drugName())
                .append(" + ").append(response.durB().drugName()).append("\n");
        sb.append("- durtags: ");
        appendDurTagsExpanded(sb, response.interact().durtags());
        sb.append("\n");

        // 프롬프트 지침
        sb.append("아래는 사용자가 복용 전 확인 중인 두 가지 약-건강기능식품간 병용 조합의 DUR 정보예요.\n")
                .append("사용자는 아직 어떤 약과 건강기능식품도 복용하지 않았으며, 복용 전 주의사항을 확인하려는 중이에요.\n\n")

                .append("모든 설명은 해요체(~요)로, 부드럽고 자연스럽게 작성해 주세요. '~니다'나 딱딱한 표현은 사용하지 말고, 환자가 이해하기 쉽게 풀어주세요.\n\n")

                .append("각 항목은 다음 지침을 따라 주세요:\n\n")

                .append("1. 약-건강기능식품 설명:\n")
                .append("- durtags가 비어 있고, isTakingOtherDrugs가 true인 경우: '지금 드시는 약들과는 특별한 상호작용이 없어요'를 넣으며 안심시키는 문장을 넣어 주세요.\n")
                .append("- durtags가 있을 경우: 모든 title 항목(예: 임부금기, 노인금기 등)을 하나도 빠짐없이 설명해 주세요.\n")
                .append("  각 title 안의 reason, note를 자연스럽게 해요체 문단으로 풀어 주세요.\n\n")

                .append("2. 병용 DUR 설명:\n")
                .append("- 절대 병용금기를 직접 판단해서 설명하지마세요. durtags만을 활용하여 설명하세요.\n")
                .append("- 두 약의 조합에 대해서만 설명하고, 다른 약들과의 관계는 언급하지 마세요.\n")
                .append("- durtags가 없으면 '두 약 사이에 특별한 상호작용은 없어요'처럼 간단하게 작성해 주세요.\n")
                .append("- durtags가 있을 경우, 부드러운 말투로 안내해 주세요. 예: '{약}와 {건강기능식품}는 함께 복용하면 안 되는 조합이에요.'\n")
                .append("- '현재 다른 약들과 문제는 없어요'라는 문장은 병용 DUR 설명에 절대 포함하지 마세요.\n\n")

                .append("[출력 조건]\n")
                .append("- 마크다운을 사용하지 마세요.\n")
                .append("- 각 설명은 줄바꿈으로 구분될 수 있도록 해주세요\n")
                .append("- 각 설명은 180자에서 200자 사이로 작성해 주세요.\n\n")

                .append("출력 형식:\n")
                .append("'{약물 이름}은' ~ 으로 시작해 해요체로 설명\n")
                .append("'{건강기능식품 이름}은' 으로 시작해 해요체로 설명\n")
                .append("마지막 문단은 두 약의 병용에 대한 설명으로 마무리해 주세요\n\n")

                .append("※ 주의: 사용자는 아직 어떤 약,건강기능식품도 복용하지 않았어요.\n")
                .append("'복용 중이시군요', '복용하고 계신다면' 같은 표현은 사용하지 마세요.\n")
                .append("'복용할 때 주의해야 해요', '복용 전에는 이런 점을 확인해 주세요' 처럼 안내해 주세요.\n");


        return sb.toString();
    }


    private void appendDurTagsExpanded(StringBuilder sb, List<DurTagDto> durtags) {
        if (durtags == null || durtags.isEmpty()) {
            sb.append("[]\n");
            return;
        }

        sb.append("[\n");
        for (DurTagDto tag : durtags) {
            sb.append("  {\n");
            sb.append("    title: ").append(tag.title()).append(",\n");
            sb.append("    durDtos: [\n");
            for (DurDto dto : tag.durDtos()) {
                sb.append("      { name: ").append(dto.name())
                        .append(", reason: ").append(dto.reason())
                        .append(", note: ").append(dto.note()).append(" },\n");
            }
            sb.append("    ]\n");
            sb.append("  },\n");
        }
        sb.append("]\n");
    }


    private String buildPrompt(SupplementPromptRequestDto dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 사용자의 상황과 복약정보에 따라 맞춤형 건강기능식품 소개를 하는 역할입니다.\n\n");

        sb.append("- DUR 정보가 존재하는 경우\n")
                .append("**해요체**로 부드럽게 경고합니다. (예: \"이 건강기능식품은 임신 중에는 복용하면 안 돼요.\")\n\n")
                .append("- DUR 정보가 비어 있는 경우**\n")
                .append("**해요체**로 효능과 해당 사용자 정보와 관련된 주의사항만 간단히 알려줍니다. \n")
                .append("사용자 정보 기반 조건 매칭이 우선, 그 외의 주의는 일반적 상황일 때만 언급\n\n")
                .append("**마크다운을 사용하지 마세요**\n")
                .append("- 문장이 자연스럽고 부드럽게 이어지도록 작성해 주세요.\n")
                .append("- 각 설명은 280자에서 300자 사이로 작성해 주세요.\n\n");

        // DUR 정보
        sb.append("DUR 정보: ");
        sb.append(dto.durInfo() == null || dto.durInfo().isEmpty() ? "\"\"" : dto.durInfo());
        sb.append("\n");

        // 사용자 정보
        sb.append("사용자 정보: { ")
                .append("닉네임 : ").append(dto.nickname()).append("\n")
                .append("나이 : ").append(dto.age()).append("\n")
                .append("성별 : ").append(dto.gender()).append("\n")
                .append("임신 여부 : ").append(dto.isPregnant()).append("\n")
                .append("알러지 : ").append(dto.allegy()).append("\n")
                .append("기저질환 : ").append(dto.underlyingDisease()).append("\n")
                .append("복용중인 약 : {");

        if (dto.currentDrugs() != null && !dto.currentDrugs().isEmpty()) {
            String drugList = dto.currentDrugs().stream()
                    .map(d -> "\"" + d + "\"")
                    .collect(Collectors.joining(", "));
            sb.append(drugList);
        }
        sb.append("}\n");
        sb.append("복용중인 건강기능식품 : {");

        if (dto.currentSupplements() != null && !dto.currentSupplements().isEmpty()) {
            String supplementList = dto.currentSupplements().stream()
                    .map(d -> "\"" + d + "\"")
                    .collect(Collectors.joining(", "));
            sb.append(supplementList);
        }
        sb.append("} }\n");
        // 약 정보
        sb.append("건강기능식품 정보: { ").append(dto.supplementInfo()).append(" }\n\n");

        // 출력 포맷 안내
        sb.append("출력 형식:\n[사용자 맞춤형 안내. 반드시 해요체. 반드시 280~300자]")
            .append("출력 시 반드시 다음 요소를 포함해야 해요:\n")
            .append("1. 건강기능식품의 **상세한 기능 또는 작용 기전** (예: 혈압을 낮춰요, 근육통을 줄여줘요 등\n")
            .append("2. **사용자 조건**과 **위험 내용**을 명확히 연결해서 설명 (예: 노인은 어지럼증 위험이 커요)")
            .append("3. 임신여부가 true라면 무조건 '{닉네임}님 임신 축하드려요 💖' 로 시작하기. false라면 '{닉네임}님' 으로 설명 시작하기");

        return sb.toString();
    }

    public DurExplanationResult parseCombinedResponse(String gptResponse) {
        // 섹션을 "[약물 A 설명]" 등의 헤더 기준으로 분리
        String[] parts = gptResponse.split("\\n");

        String[] dur = new String[3];
        int idx = 0;
        for(String str : parts) {
            str = str.trim();
            if(!str.isEmpty())
                dur[idx++] = str;
            if(idx==3) break;
        }

        return new DurExplanationResult(dur[0],dur[1],dur[2]);
    }

}
