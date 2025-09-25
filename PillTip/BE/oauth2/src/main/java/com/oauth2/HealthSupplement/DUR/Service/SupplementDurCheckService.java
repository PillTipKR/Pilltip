package com.oauth2.HealthSupplement.DUR.Service;

import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.TakingSupplement.Dto.TakingSupplementSummaryResponse;
import com.oauth2.User.TakingSupplement.Service.TakingSupplementService;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplementDurCheckService {

    private final StringRedisTemplate redisTemplate;
    private final HealthSupplementRepository healthSupplementRepository;
    private final TakingSupplementService takingSupplementService;

    @Value("${redis.supplement.tag}")
    private String supplementTag;

    @Value("${redis.drug.tag}")
    private String drugTag;

    @Value("${redis.inter.tag}")
    private String interTag;

    // 사용자가 먹는 건기식 기준 컨텍스트 생성
    public DurUserContext buildUserContext(User user) {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToSupplementIdsMap = new HashMap<>();
        Set<String> userInteractionSupplementNames = new HashSet<>();


        // 건기식 등록으로 변경하기!
        List<Long> supplementIds = takingSupplementService.getTakingSupplementSummary(user).getTakingSupplements().stream()
                .map(TakingSupplementSummaryResponse.TakingSupplementSummary::getSupplementId)
                .toList();

        for (Long userSupplementId : supplementIds) {
            Optional<HealthSupplement> userSupplementOpt = healthSupplementRepository.findById(userSupplementId);
            if (userSupplementOpt.isEmpty()) continue;

            String supplementName = userSupplementOpt.get().getProductName();
            List<String> contraDrugList = redisTemplate.opsForList().range(supplementTag+"-"+drugTag+interTag + supplementName, 0, -1);
            List<String> supplementContraList = redisTemplate.opsForList().range(supplementTag+interTag, 0, -1);
            if (contraDrugList != null && !contraDrugList.isEmpty()) userInteractionSupplementNames.add(supplementName);
            if (supplementContraList != null && !supplementContraList.isEmpty()) userInteractionSupplementNames.add(supplementName);
        }
        return new DurUserContext(isElderly, user.getUserProfile().isPregnant(), classToSupplementIdsMap, userInteractionSupplementNames);
    }
}
