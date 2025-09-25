package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Domain.DurEntity;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
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

    @Value("${redis.inter.tag}")
    private String interTag;

    @Value("${redis.inter.detail.tag}")
    private String interDetailTag;

    @Value("${redis.drug.tag}")
    private String drugTag;

    @Value("${redis.supplement.tag}")
    private String supplementTag;

    @Value("${redis.drug.ingr.tag}")
    private String drugIngrTag;

    @Value("${redis.supplement.ingr.tag}")
    private String supplementIngrTag;

    public List<DurTagDto> checkForWithoutInteraction(Long id,
                                                      DurType durType,
                                                      UserProfile userProfile,
                                                      DurUserContext userContext) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        String key = "";
        switch(durType){
            case DRUG -> key=drugTag;
            case DRUGINGR -> key=drugIngrTag;
            case SUPPLEMENT -> key=supplementTag;
            default -> key=supplementIngrTag;
        }

        // 임부금기
        String pregTag = ":DUR:PREGNANCY:";
        tags.add(buildDurTag("임부금기", readJsonFromRedis(key+ pregTag + id), userProfile.isPregnant()));

        // 노인금기
        String elderTag = ":DUR:ELDER:";
        tags.add(buildDurTag("노인금기", readJsonFromRedis(key+ elderTag + id), userContext.isElderly()));

        // 연령금기
        String ageTag = ":DUR:AGE:";
        Map<String, String> ageValue = readJsonFromRedis(key+ ageTag + id);
        boolean showAgeTag = false;
        if(key.equals(drugTag))
            showAgeTag = ageValue != null && isUserInRestrictedAge(userProfile.getBirthDate(), ageValue.get("conditionValue"));
        else if(key.equals(supplementTag))
            showAgeTag = ageValue != null && userProfile.getAge() <= 12;
        tags.add(buildDurTag("연령금기", ageValue, showAgeTag));

        // 효능군 중복주의
        Map<String, String> therValue = readJsonFromRedis(key+":DUR:THERAPEUTIC_DUP:" + id);
        String className = therValue != null ? therValue.get("className") : null;
        boolean isDup = className != null && userContext.classToProductIdsMap().containsKey(className);
        tags.add(buildDurTag("효능군중복주의", therValue, isDup));

        return tags;
    }

    public List<DurTagDto> checkForInteractions(DurEntity durEntity,
                                                DurType durType,
                                                UserProfile userProfile,
                                                DurUserContext drugUserContext,
                                                DurUserContext supplementUserContext) throws JsonProcessingException {

        List<DurTagDto> tags = checkForWithoutInteraction(durEntity.getId(),durType,userProfile,drugUserContext);
        String name = durEntity.getName();

        String key1 = "";
        String key2 = switch (durType) {
            case DRUG -> {
                key1 = drugTag + interDetailTag;
                yield supplementTag + "-" + drugTag + interDetailTag;
            }
            case SUPPLEMENT -> {
                key1 = supplementTag + "-" + drugTag + interDetailTag;
                yield supplementTag + interDetailTag;
            }
            case DRUGINGR -> {
                key1 = drugTag + "-" + drugIngrTag + interDetailTag;
                yield supplementTag + "-" + drugIngrTag + interDetailTag;
            }
            default -> {
                key1 = drugTag + "-" + supplementIngrTag + interDetailTag;
                yield supplementTag + "-" + supplementIngrTag + interDetailTag;
            }
        };

        tags.add(
                buildContraTag(name,
                        drugUserContext.userInteractionProductNames(),
                        supplementUserContext.userInteractionProductNames(),
                        key1,
                        key2
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
            List<String> drugContraList = redisTemplate.opsForList().range(drugTag + interTag + drugName, 0, -1);
            List<String> supplementContraList = redisTemplate.opsForList().range(supplementTag + interTag + drugName, 0, -1);
            if (drugContraList != null && !drugContraList.isEmpty()) userInteractionDrugNames.add(drugName);
            if(supplementContraList != null && !supplementContraList.isEmpty()) userInteractionDrugNames.add(drugName);


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


    // 반복하면서 방향 정보까지 넘겨줌
    public void collectInteractionTags(String productName, Set<String> others, String tag,List<DurDto> tagDesc) throws JsonProcessingException {
        for (String otherName : others) {
            String key = productName + ":" + otherName;
            String detailKey = tag + key;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            if (detail != null) {
                tagDesc.add(new DurDto(
                        productName + ":" + otherName,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
        }
    }

    public DurTagDto buildContraTag(
            String name,
            Set<String> userInteractionDrugNames,
            Set<String> userInteractionSupplementNames,
            String tag1,
            String tag2
    ) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();

        collectInteractionTags(name, userInteractionDrugNames, tag1, tagDesc);
        collectInteractionTags(name, userInteractionSupplementNames, tag2, tagDesc);

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
