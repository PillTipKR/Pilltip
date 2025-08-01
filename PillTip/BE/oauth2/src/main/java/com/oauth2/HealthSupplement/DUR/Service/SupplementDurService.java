package com.oauth2.HealthSupplement.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.*;
import com.oauth2.Drug.DUR.Service.DurCheckService;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.HealthSupplement.DUR.Dto.SupplementDurAnalysisResponse;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplementDurService {

    private final DrugRepository drugRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final SupplementDurCheckService supplementDurCheckService;
    private final DurCheckService durCheckService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    //건기식과 약 간의 상충작용비교 ( 필요하다면 사용하기 )
    public SupplementDurAnalysisResponse generateTagsForSupplementAndDrug(User user, long supplementId, long drugId) throws JsonProcessingException {
        Optional<HealthSupplement> supplements = healthSupplementRepository.findById(supplementId);
        Optional<Drug> drugs = drugRepository.findById(drugId);

        if (supplements.isEmpty() || drugs.isEmpty()) {
            // 약 정보를 찾을 수 없는 경우 예외 처리 또는 기본 응답 반환
            throw new NoSuchElementException("One or both drugs not found");
        }

        DurUserContext supplementUserContext = supplementDurCheckService.buildUserContext(user);
        DurUserContext drugUserContext = durCheckService.buildUserContext(user);

        List<DurTagDto> tagsForSupplement = supplementDurCheckService.checkForSupplementAndDrug(supplements.get(), user.getUserProfile(), supplementUserContext, drugUserContext);
        List<DurTagDto> tagsForDrug = durCheckService.checkForDrugAndSupplement(drugs.get(), user.getUserProfile(), drugUserContext,supplementUserContext);
        List<DurTagDto> interactionTags = checkInteractionBetweenSupplementAndDrug(supplements.get(), drugs.get());

        String supplementName = removeParentheses(supplements.get().getProductName());
        String drugName = removeParentheses(drugs.get().getName());
        return new SupplementDurAnalysisResponse(
                new DurPerProductDto(
                        supplementName,
                        tagsForSupplement.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(
                        drugName,
                        tagsForDrug.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(
                        supplementName + " + " + drugName,
                        interactionTags.stream().filter(DurTagDto::isTrue).toList()),
                !supplementUserContext.userInteractionProductNames().isEmpty()
        );
    }

    private List<DurTagDto> checkInteractionBetweenSupplementAndDrug(HealthSupplement supplement, Drug drug) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        String supplementName = supplement.getProductName();
        String drugName = drug.getName();

        // 병용금기 확인
        List<String> contraList = redisTemplate.opsForList().range("SUPPLEMENT-DRUG:DUR:INTERACT:" + supplementName, 0, -1);
        if (contraList != null && contraList.contains(drugName)) {
            String detailKey = "SUPPLEMENT-DRUG:DUR:INTERACT_DETAIL:" + supplementName + ":" + drugName;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            List<DurDto> tagDesc = new ArrayList<>();
            if (detail != null) {
                tagDesc.add(new DurDto(
                        supplementName + " + " + drugName,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
            tags.add(new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty()));
        }

        return tags;
    }

    private String removeParentheses(String text) {
        if (text == null) {
            System.out.println("[removeParentheses] text is null!");
            return "";
        }
        return text.replaceAll("(\\(.*?\\)|\\[.*?\\])", "").trim();
    }

    private Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
    }
}
