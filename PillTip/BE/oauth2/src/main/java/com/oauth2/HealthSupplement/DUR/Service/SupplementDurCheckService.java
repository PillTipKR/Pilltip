package com.oauth2.HealthSupplement.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DUR.Service.DurCheckService;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplementDurCheckService {

    private final StringRedisTemplate redisTemplate;
    private final DurCheckService durCheckService;
    private final HealthSupplementRepository healthSupplementRepository;
    private final TakingPillService takingPillService;

    @Value("${redis.supplement.drug.detail.tag}")
    private String supplementDrugDetailTag;

    @Value("${redis.supplement.inter.detail.tag}")
    private String supplementInterDetailTag;

    @Value("${redis.supplement.drug.tag}")
    private String supDrugTag;

    @Value("${redis.supplement.inter.tag}")
    private String supplementInterTag;

    // 금기가 있는지 확인
    public List<DurTagDto> checkForSupplementWithoutInteraction(HealthSupplement supplement, UserProfile userProfile, DurUserContext userContext) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        Long supplementId = supplement.getId();

        // 임부금기
        tags.add(durCheckService.buildDurTag("임부금기", durCheckService.readJsonFromRedis("SUPPLEMENT:DUR:PREGNANCY:" + supplementId), userProfile.isPregnant()));

        // 노인금기
        tags.add(durCheckService.buildDurTag("노인금기", durCheckService.readJsonFromRedis("SUPPLEMENT:DUR:ELDER:" + supplementId), userContext.isElderly()));

        // 연령금기
        Map<String, String> ageValue = durCheckService.readJsonFromRedis("SUPPLEMENT:DUR:AGE:" + supplementId);
        boolean showAgeTag = ageValue != null && durCheckService.isUserInRestrictedAge(userProfile.getBirthDate(), ageValue.get("conditionValue"));
        tags.add(durCheckService.buildDurTag("연령금기", ageValue, showAgeTag));

        return tags;
    }

    public List<DurTagDto> checkForSupplementAndDrug(HealthSupplement supplement, UserProfile userProfile,
                                                     DurUserContext supplementUserContext, DurUserContext drugUserContext) throws JsonProcessingException {
        List<DurTagDto> tags = checkForSupplementWithoutInteraction(supplement, userProfile, supplementUserContext);
        String supplementName = supplement.getProductName();

        // 병용금기 (사용자가 복용중인 다른 약물과의 상호작용)
        tags.add(
                buildSupplementDrugContraTag(supplementName,
                        drugUserContext.userInteractionProductNames(),
                        supplementUserContext.userInteractionProductNames(),
                        supplementDrugDetailTag,
                        supplementInterDetailTag
                ));
        return tags;
    }

    // 사용자가 먹는 건기식 기준 컨텍스트 생성
    public DurUserContext buildUserContext(User user) {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToSupplementIdsMap = new HashMap<>();
        Set<String> userInteractionSupplementNames = new HashSet<>();


        // 건기식 등록으로 변경하기!
        List<Long> supplementIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        for (Long userSupplementId : supplementIds) {
            Optional<HealthSupplement> userSupplementOpt = healthSupplementRepository.findById(userSupplementId);
            if (userSupplementOpt.isEmpty()) continue;

            String supplementName = userSupplementOpt.get().getProductName();
            List<String> contraDrugList = redisTemplate.opsForList().range(supDrugTag + supplementName, 0, -1);
            List<String> supplementContraList = redisTemplate.opsForList().range(supplementInterTag, 0, -1);
            if (contraDrugList != null && !contraDrugList.isEmpty()) userInteractionSupplementNames.add(supplementName);
            if (supplementContraList != null && !supplementContraList.isEmpty()) userInteractionSupplementNames.add(supplementName);
        }
        return new DurUserContext(isElderly, user.getUserProfile().isPregnant(), classToSupplementIdsMap, userInteractionSupplementNames);
    }


    private DurTagDto buildSupplementDrugContraTag(String supplementName,
                                                   Set<String> userInteractionDrugNames,
                                                   Set<String> userInteractionSupplementNames,
                                                   String tag1,
                                                   String tag2) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();

        // 건강기능식품 - 약품
        durCheckService.collectInteractionTags(supplementName, userInteractionDrugNames, tag1, false, tagDesc);

        // 건강기능식품 - 건강기능식품
        durCheckService.collectInteractionTags(supplementName, userInteractionSupplementNames, tag2, false, tagDesc);

        return new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty());
    }


}
