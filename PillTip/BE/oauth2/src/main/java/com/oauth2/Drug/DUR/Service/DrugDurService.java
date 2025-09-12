package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.*;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.HealthSupplement.DUR.Service.SupplementDurCheckService;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DrugDurService {

    private final DrugRepository drugRepository;
    private final DurCheckService durCheckService;
    private final SupplementDurCheckService supplementDurCheckService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${redis.drug.inter.tag}")
    private String drugInterTag;

    @Value("${redis.drug.inter.detail.tag}")
    private String drugInterDetailTag;

    public DurAnalysisResponse generateTagsForDrugs(User user, long drugId1, long drugId2) throws JsonProcessingException {
        Optional<Drug> drug1Opt = drugRepository.findById(drugId1);
        Optional<Drug> drug2Opt = drugRepository.findById(drugId2);

        if (drug1Opt.isEmpty() || drug2Opt.isEmpty()) {
            // 약 정보를 찾을 수 없는 경우 예외 처리 또는 기본 응답 반환
            throw new NoSuchElementException("One or both drugs not found");
        }

        DurUserContext drugUserContext = durCheckService.buildUserContext(user);
        DurUserContext supplementUserContext = supplementDurCheckService.buildUserContext(user);

        List<DurTagDto> tagsForDrug1 = durCheckService.checkForDrugAndSupplement(drug1Opt.get(), user.getUserProfile(), drugUserContext,supplementUserContext);
        List<DurTagDto> tagsForDrug2 = durCheckService.checkForDrugAndSupplement(drug2Opt.get(), user.getUserProfile(), drugUserContext,supplementUserContext);
        List<DurTagDto> interactionTags = checkInteractionBetweenTwoDrugs(drug1Opt.get(), drug2Opt.get());

        String drugName1 = removeParentheses(drug1Opt.get().getName());
        String drugName2 = removeParentheses(drug2Opt.get().getName());
        return new DurAnalysisResponse(
                new DurPerProductDto(
                        drugName1,
                        tagsForDrug1.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(
                        drugName2,
                        tagsForDrug2.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(
                        drugName1 + " + " + drugName2,
                        interactionTags.stream().filter(DurTagDto::isTrue).toList()),
                !drugUserContext.userInteractionProductNames().isEmpty()
        );
    }

    private List<DurTagDto> checkInteractionBetweenTwoDrugs(Drug drug1, Drug drug2) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        String drugName1 = drug1.getName();
        String drugName2 = drug2.getName();

        // 병용금기 확인
        List<String> contraList = redisTemplate.opsForList().range(drugInterTag + drugName1, 0, -1);
        if (contraList != null && contraList.contains(drugName2)) {
            String detailKey = drugInterDetailTag + drugName1 + ":" + drugName2;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            List<DurDto> tagDesc = new ArrayList<>();
            if (detail != null) {
                tagDesc.add(new DurDto(
                        drugName1 + " + " + drugName2,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
            tags.add(new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty()));
        }

        // 효능군 중복 확인
        Map<String, String> therValue1 = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + drug1.getId());
        Map<String, String> therValue2 = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + drug2.getId());

        if (therValue1 != null && therValue2 != null) {
            String className1 = therValue1.get("className");
            String className2 = therValue2.get("className");
            if (className1 != null && className1.equals(className2)) {
                List<DurDto> list = new ArrayList<>();
                list.add(new DurDto(
                    therValue1.getOrDefault("category", ""),
                    therValue1.getOrDefault("conditionValue", therValue1.getOrDefault("remark", "")),
                    therValue1.getOrDefault("note", "")
                ));
                tags.add(new DurTagDto("효능군중복주의", list, true));
            }
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
