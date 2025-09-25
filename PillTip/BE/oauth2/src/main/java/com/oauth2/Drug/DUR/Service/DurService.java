package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Domain.DurEntity;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Dto.*;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.HealthSupplement.DUR.Service.SupplementDurCheckService;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
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
public class DurService {

    private final DrugRepository drugRepository;
    private final IngredientRepository ingredientRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final DurCheckService durCheckService;
    private final SupplementDurCheckService supplementDurCheckService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${redis.supplement.tag}")
    private String supplementTag;

    @Value("${redis.drug.tag}")
    private String drugTag;

    @Value("${redis.inter.tag}")
    private String interTag;

    @Value("${redis.inter.detail.tag}")
    private String interDetailTag;

    @Value("${redis.drug.ingr.tag}")
    private String drugIngrTag;

    @Value("${redis.supplement.ingr.tag}")
    private String supIngrTag;

    public DurAnalysisResponse generateTagsForDurEntities(
            User user,
            Long entityId1,
            Long entityId2,
            DurType type1,
            DurType type2
    ) throws JsonProcessingException {

        DurEntity entity1 = getDurEntity(type1, entityId1);
        DurEntity entity2 = getDurEntity(type2, entityId2);

        if (entity1 == null || entity2 == null) {
            throw new NoSuchElementException("One or both entities not found");
        }

        String name1 = removeParentheses(entity1.getName());
        String name2 = removeParentheses(entity2.getName());


        DurUserContext drugUserContext = durCheckService.buildUserContext(user);
        DurUserContext supplementUserContext = supplementDurCheckService.buildUserContext(user);

        List<DurTagDto> tagsForEntity1 = durCheckService.checkForInteractions(entity1, type1,user.getUserProfile(), drugUserContext, supplementUserContext);
        List<DurTagDto> tagsForEntity2 = durCheckService.checkForInteractions(entity2, type2,user.getUserProfile(), drugUserContext, supplementUserContext);
        List<DurTagDto> interactionTags = checkInteractionBetweenTwoEntities(entity1, entity2, type1,type2);

        return new DurAnalysisResponse(
                new DurPerProductDto(name1, tagsForEntity1.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(name2, tagsForEntity2.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerProductDto(name1 + " + " + name2, interactionTags.stream().filter(DurTagDto::isTrue).toList()),
                !drugUserContext.userInteractionProductNames().isEmpty()
        );
    }

    private List<DurTagDto> checkInteractionBetweenTwoEntities(DurEntity e1, DurEntity e2, DurType type1, DurType type2) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        String name1 = e1.getName();
        String name2 = e2.getName();
        String prefix;
        // 병용금기 Redis 키 구성
        if(type1.equals(DurType.DRUG) && type2.equals(DurType.DRUG)){
            prefix = drugTag;
        }
        else if((type1.equals(DurType.DRUG) && type2.equals(DurType.SUPPLEMENT))
                || (type1.equals(DurType.SUPPLEMENT) && type2.equals(DurType.DRUG))){
            prefix = supplementTag+"-"+drugTag;
        }
        else if((type1.equals(DurType.DRUG) && type2.equals(DurType.DRUGINGR))
                || (type1.equals(DurType.DRUGINGR) && type2.equals(DurType.DRUG))){
            prefix = drugTag+"-"+drugIngrTag;
        }
        else if((type1.equals(DurType.DRUG) && type2.equals(DurType.SUPINGR))
                || (type1.equals(DurType.SUPINGR) && type2.equals(DurType.DRUG))){
            prefix = drugTag+"-"+supIngrTag;
        }
        else if((type1.equals(DurType.SUPPLEMENT) && type2.equals(DurType.SUPPLEMENT))){
            prefix = supplementTag;
        }
        else if((type1.equals(DurType.DRUGINGR) && type2.equals(DurType.DRUGINGR))){
            prefix = drugIngrTag+"-"+supIngrTag;
        }
        else if((type1.equals(DurType.SUPINGR) && type2.equals(DurType.SUPINGR))){
            prefix = supIngrTag;
        }
        else if((type1.equals(DurType.SUPPLEMENT) && type2.equals(DurType.DRUGINGR))
                || (type1.equals(DurType.DRUGINGR) && type2.equals(DurType.SUPPLEMENT))){
            prefix = drugIngrTag+"-"+supIngrTag;
        }
        else if((type1.equals(DurType.SUPPLEMENT) && type2.equals(DurType.SUPINGR))
                || (type1.equals(DurType.SUPINGR) && type2.equals(DurType.SUPPLEMENT))){
            prefix = supplementTag+"-"+supIngrTag;
        }
        else {
            prefix = drugIngrTag+"-"+supIngrTag;
        }


        List<String> contraList = redisTemplate.opsForList().range(prefix+interTag + name1, 0, -1);
        if (contraList != null && contraList.contains(name2)) {
            String detailKey = prefix+interDetailTag + name1 + ":" + name2;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            List<DurDto> tagDesc = new ArrayList<>();
            if (detail != null) {
                tagDesc.add(new DurDto(
                        name1 + " + " + name2,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
            tags.add(new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty()));
        }

        // 효능군 중복 확인
        if(type1.equals(DurType.DRUG) && type2.equals(DurType.DRUG)) {
            Map<String, String> therValue1 = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + e1.getId());
            Map<String, String> therValue2 = readJsonFromRedis("DRUG:DUR:THERAPEUTIC_DUP:" + e2.getId());

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
        }
        return tags;
    }


    // getDurEntity 메소드 작성
    private DurEntity getDurEntity(DurType type, long id) {
        return switch (type) {
            case DRUG -> drugRepository.findById(id).orElse(null);  // Drug 객체 반환
            case SUPPLEMENT -> healthSupplementRepository.findById(id).orElse(null);  // HealthSupplement 객체 반환
            case DRUGINGR -> ingredientRepository.findById(id).orElse(null);  // Ingredient 객체 반환
            default -> null;  // 그 외는 null 반환
        };
    }


    public DurEntity getDurEntityByName(DurType type, String name) {
        return switch (type) {
            case DRUG -> drugRepository.findByName(name).orElse(null);  // Drug 객체 반환
            case SUPPLEMENT -> healthSupplementRepository.findByProductName(name).orElse(null);  // HealthSupplement 객체 반환
            case DRUGINGR -> ingredientRepository.findByNameKr(name).orElse(null);  // Ingredient 객체 반환
            default -> null;  // 그 외는 null 반환
        };
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
