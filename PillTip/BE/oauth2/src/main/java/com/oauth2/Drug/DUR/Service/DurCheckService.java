package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DurCheckService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;

    @Value("${redis.supplement.drug.detail.tag}")
    private String supDrugDetailTag;

    @Value("${redis.drug.inter.detail.tag}")
    private String drugDetailTag;

    @Value("${redis.drug.inter.tag}")
    private String drugInterTag;

    public List<DurTagDto> checkForWithoutInteraction(Drug drug, UserProfile userProfile, DurUserContext userContext) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        Long drugId = drug.getId();

        // 임부금기
        tags.add(buildDurTag("임부금기", readJsonFromRedis("DRUG:DUR:PREGNANCY:" + drugId), userProfile.isPregnant()));

        // 노인금기
        tags.add(buildDurTag("노인금기", readJsonFromRedis("DRUG:DUR:ELDER:" + drugId), userContext.isElderly()));

        // 연령금기
        Map<String, String> ageValue = readJsonFromRedis("DRUG:DUR:AGE:" + drugId);
        boolean showAgeTag = ageValue != null && isUserInRestrictedAge(userProfile.getBirthDate(), ageValue.get("conditionValue"));
        tags.add(buildDurTag("연령금기", ageValue, showAgeTag));

        // 효능군 중복주의
        Map<String, String> therValue = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + drugId);
        String className = therValue != null ? therValue.get("className") : null;
        boolean isDup = className != null && userContext.classToProductIdsMap().containsKey(className);
        tags.add(buildDurTag("효능군중복주의", therValue, isDup));

        return tags;
    }

    public List<DurTagDto> checkForDrugAndSupplement(Drug drug, UserProfile userProfile,
                                        DurUserContext drugUserContext, DurUserContext supplementUserContext) throws JsonProcessingException {
        List<DurTagDto> tags = checkForWithoutInteraction(drug,userProfile,drugUserContext);
        String drugName = drug.getName();

        tags.add(
                buildDrugSupplementContraTag(drugName,
                drugUserContext.userInteractionProductNames(),
                supplementUserContext.userInteractionProductNames(),
                supDrugDetailTag,
                drugDetailTag
        ));

        return tags;
    }

    public Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
    }

    public DurUserContext buildUserContext(User user) throws JsonProcessingException {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteractionDrugNames = new HashSet<>();

        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        for (Long userDrugId : userDrugIds) {
            Optional<Drug> userDrugOpt = drugRepository.findById(userDrugId);
            if (userDrugOpt.isEmpty()) continue;

            String drugName = userDrugOpt.get().getName();
            List<String> contraList = redisTemplate.opsForList().range(drugInterTag + drugName, 0, -1);
            if (contraList != null && !contraList.isEmpty()) {
                userInteractionDrugNames.add(drugName);
            }

            Map<String, String> value = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + userDrugId);
            if (value != null) {
                String className = value.getOrDefault("className", "").trim();
                if (!className.isBlank()) {
                    classToDrugIdsMap.computeIfAbsent(className, k -> new ArrayList<>()).add(userDrugId);
                }
            }
        }
        return new DurUserContext(isElderly, user.getUserProfile().isPregnant(), classToDrugIdsMap, userInteractionDrugNames);
    }

    public DurTagDto buildDurTag(String tagName, Map<String, String> valueMap, boolean shouldTag) {
        List<DurDto> list = new ArrayList<>();
        if (shouldTag && valueMap != null && !valueMap.isEmpty()) {
            list.add(new DurDto(
                    valueMap.getOrDefault("category", ""),
                    valueMap.getOrDefault("conditionValue", valueMap.getOrDefault("remark", "")),
                    valueMap.getOrDefault("note", "")
            ));
        }
        return new DurTagDto(tagName, list, shouldTag && !list.isEmpty());
    }

    // 방향 플래그를 받아서 detailKey 생성
    private void tryAddInteraction(String name1, String tag, String name2, boolean reverseKey, List<DurDto> tagDesc) throws JsonProcessingException {
        String key = reverseKey ? (name1 + ":" + name2) : (name2 + ":" + name1);
        String detailKey = tag + key;

        Map<String, String> detail = readJsonFromRedis(detailKey);
        if (detail != null) {
            tagDesc.add(new DurDto(
                    name1 + " + " + name2,
                    detail.getOrDefault("reason", ""),
                    detail.getOrDefault("note", "")
            ));
        }
    }

    // 반복하면서 방향 정보까지 넘겨줌
    public void collectInteractionTags(String drugName, Set<String> others, String tag, boolean reverseKey, List<DurDto> tagDesc) throws JsonProcessingException {
        for (String otherName : others) {
            tryAddInteraction(otherName, tag, drugName, reverseKey, tagDesc);
        }
    }

    // 메인: 각각 방향 다르게 설정
    public DurTagDto buildDrugSupplementContraTag(
            String drugName,
            Set<String> userInteractionDrugNames,
            Set<String> userInteractionSupplementNames,
            String tag1,
            String tag2
    ) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();

        // 건강기능식품 → drugName이 뒤에 (reverseKey = true)
        collectInteractionTags(drugName, userInteractionSupplementNames, tag1, true, tagDesc);

        // 약물 → drugName이 앞에 (reverseKey = false)
        collectInteractionTags(drugName, userInteractionDrugNames, tag2, false, tagDesc);

        return new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty());
    }


    public boolean isUserInRestrictedAge(LocalDate birthDate, String conditionValue) {
        if (conditionValue == null || birthDate == null || conditionValue.isBlank()) return false;
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        int ageInMonths = age * 12 + Period.between(birthDate, today).getMonths();

        String[] parts = conditionValue.split("\\s*,\\s*");
        for (String part : parts) {
            int limit;
            try {
                limit = Integer.parseInt(part.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            if (part.contains("개월 미만") && ageInMonths < limit) return true;
            if (part.contains("개월 이하") && ageInMonths <= limit) return true;
            if (part.contains("개월 초과") && ageInMonths > limit) return true;
            if (part.contains("개월 이상") && ageInMonths >= limit) return true;
            if (part.contains("세 미만") && age < limit) return true;
            if (part.contains("세 이하") && age <= limit) return true;
            if (part.contains("세 초과") && age > limit) return true;
            if (part.contains("세 이상") && age >= limit) return true;
        }
        return false;
    }
}
