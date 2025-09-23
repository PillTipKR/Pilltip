package com.oauth2.Util.Redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectCaution;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import com.oauth2.Drug.DUR.Domain.DrugTherapeuticDup;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DUR.Repository.SubjectCautionRepository;
import com.oauth2.Drug.DUR.Repository.SubjectInteractionRepository;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DUR.Repository.DrugTherapeuticDupRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DurRedisLoader {

    private final StringRedisTemplate redisTemplate;
    private final SubjectInteractionRepository subjectInteractionRepository;
    private final SubjectCautionRepository subjectCautionRepository;
    private final DrugTherapeuticDupRepository dupRepo;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final IngredientRepository ingredientRepository;

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
    private String supIngrTag;


    public void loadAll() throws JsonProcessingException {
        saveAllInteractions();
        saveAllCautions();
        saveTherapeuticDups();
    }

    private void saveAllCautions() throws JsonProcessingException {
        saveCautions(DurType.DRUG);
        saveCautions(DurType.SUPPLEMENT);
        saveCautions(DurType.DRUGINGR);
        saveCautions(DurType.SUPINGR);
    }

    private void saveInteractions(
            DurType type1,
            DurType type2,
            String detailKeyPrefix,
            String listKeyPrefix,
            Function<Long, String> nameResolver1,
            Function<Long, String> nameResolver2
    ) throws JsonProcessingException {

        List<SubjectInteraction> interactions = subjectInteractionRepository.findByDurtype1AndDurtype2(type1, type2);
        Map<String, List<String>> map = new HashMap<>();

        for (SubjectInteraction si : interactions) {
            String name1 = nameResolver1.apply(si.getSubjectId1());
            String name2 = nameResolver2.apply(si.getSubjectId2());

            if (name1 == null || name2 == null) continue;

            // Redis 리스트 저장용 맵 구성
            map.computeIfAbsent(name1, k -> new ArrayList<>()).add(name2);
            pushKey(map, listKeyPrefix);

            map.computeIfAbsent(name2, k -> new ArrayList<>()).add(name1);
            pushKey(map, listKeyPrefix);

            // Redis 상세 정보 저장
            String detailKey = detailKeyPrefix + name1 + ":" + name2;
            String detailRevKey = detailKeyPrefix + name2 + ":" + name1;
            Map<String, String> value = Map.of(
                    "reason", Optional.ofNullable(si.getReason()).orElse(""),
                    "note", Optional.ofNullable(si.getNote()).filter(n -> !n.equals("없음")).orElse("")
            );
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(detailKey, json);
            redisTemplate.opsForValue().set(detailRevKey, json);
        }
    }

    private void saveAllInteractions() throws JsonProcessingException {
        Map<Long, String> drugIdNameMap = drugRepository.findAll().stream()
                .collect(Collectors.toMap(Drug::getId, Drug::getName));
        Map<Long, String> ingrIdNameMap = ingredientRepository.findAll().stream()
                .collect(Collectors.toMap(Ingredient::getId, Ingredient::getNameKr));
        Map<Long, String> supplementIdNameMap = healthSupplementRepository.findAll().stream()
                .collect(Collectors.toMap(HealthSupplement::getId, HealthSupplement::getProductName));

        //약-약
        saveInteractions(
                DurType.DRUG,
                DurType.DRUG,
                drugTag+interDetailTag,
                drugTag+interTag,
                drugIdNameMap::get,
                drugIdNameMap::get
        );

        //약-건기식
        saveInteractions(
                DurType.DRUG,
                DurType.SUPPLEMENT,
                supplementTag+"-"+drugTag+interDetailTag,
                supplementTag+"-"+drugTag+interTag,
                drugIdNameMap::get,
                supplementIdNameMap::get
        );

        //약-약성분
        saveInteractions(
                DurType.DRUG,
                DurType.DRUGINGR,
                drugTag+"-"+drugIngrTag+interDetailTag,
                drugTag+"-"+drugIngrTag+interTag,
                drugIdNameMap::get,
                ingrIdNameMap::get
        );

        //약-건기식성분
        saveInteractions(
                DurType.DRUG,
                DurType.SUPINGR,
                drugTag+"-"+supIngrTag+interDetailTag,
                drugTag+"-"+supIngrTag+interTag,
                drugIdNameMap::get,
                ingrIdNameMap::get
        );

        //약성분-약성분
        saveInteractions(
                DurType.DRUGINGR,
                DurType.DRUGINGR,
                drugIngrTag+interDetailTag,
                drugIngrTag+interTag,
                ingrIdNameMap::get,
                ingrIdNameMap::get
        );

        //약성분-건기식
        saveInteractions(
                DurType.SUPPLEMENT,
                DurType.DRUGINGR,
                supplementTag+"-"+drugIngrTag+interDetailTag,
                supplementTag+"-"+drugIngrTag+interTag,
                drugIdNameMap::get,
                supplementIdNameMap::get
        );

        //약성분-건기식성분
        saveInteractions(
                DurType.DRUGINGR,
                DurType.SUPINGR,
                drugIngrTag+"-"+supIngrTag+interDetailTag,
                drugIngrTag+"-"+supIngrTag+interTag,
                drugIdNameMap::get,
                supplementIdNameMap::get
        );
    }

    private void pushKey(Map<String, List<String>> map, String tag) {
        for (var entry : map.entrySet()) {
            String key = tag + entry.getKey();
            List<String> ids = entry.getValue().stream().map(String::valueOf).toList();
            redisTemplate.delete(key);
            redisTemplate.opsForList().rightPushAll(key, ids);
        }
    }

    private void saveCautions(DurType durType) throws JsonProcessingException {
        List<SubjectCaution> cautions = subjectCautionRepository.findByDurtype(durType);
        String type = switch (durType) {
            case SUPPLEMENT -> "SUPPLEMENT";
            case DRUG -> "DRUG";
            case DRUGINGR -> "DRUGINGR";
            default -> "SUPINGR";
        };
        for (SubjectCaution sc : cautions) {
            String key = type + ":DUR:" + sc.getConditionType().name() + ":" + sc.getSubjectId();
            Map<String, String> value = Map.of(
                    "conditionValue", sc.getConditionValue() == null ? "" : sc.getConditionValue(),
                    "note", sc.getNote() == null ? "" : sc.getNote()
            );
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        }
    }

    private void saveTherapeuticDups() throws JsonProcessingException {
        List<DrugTherapeuticDup> dups = dupRepo.findAll();

        for (DrugTherapeuticDup dup : dups) {
            String key = "DRUG:DUR:THERAPEUTIC_DUP:" + dup.getDrugId();
            Map<String, String> value = Map.of(
                    "category", dup.getCategory(),
                    "className", dup.getClassName(),
                    "note", dup.getNote() == null ? "" : dup.getNote(),
                    "remark", dup.getRemark() == null ? "" : dup.getRemark()
            );
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        }
    }
}
