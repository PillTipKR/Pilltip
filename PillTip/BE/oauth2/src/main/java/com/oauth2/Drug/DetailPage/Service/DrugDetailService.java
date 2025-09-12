package com.oauth2.Drug.DetailPage.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DUR.Service.DrugDurTaggingService;
import com.oauth2.Drug.DetailPage.Dto.*;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Domain.DrugStorageCondition;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Entity.TakingPillCounter;
import com.oauth2.User.TakingPill.Repositoty.TakingPillCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DrugDetailService {

    private final DrugRepository drugRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final DrugDurTaggingService drugDurTaggingService;
    private final TakingPillCounterRepository takingPillCounterRepository;

    @Value("${elastic.drug.id}")
    private String drugId;

    @Value("${elastic.allSearch}")
    private String allSearch;

    public DrugDetail getDetail(User user, Long id) throws IOException {
        SearchDurDto searchDurDto = drugDurTaggingService.generateTagsForDrugs(user, getDetailFromElasticsearch(id)).get(0);

        // 한 번의 쿼리로 Drug과 관련된 DrugEffect, DrugStorageCondition을 가져옵니다.
        Optional<Drug> drug = drugRepository.findDrugWithAllRelations(id);
        Set<DrugEffect> effectDetails = new HashSet<>();
        Set<DrugStorageCondition> storageDetails = new HashSet<>();
        if(drug.isPresent()){
            effectDetails = drug.get().getDrugEffects();
            storageDetails = drug.get().getStorageConditions();
        }
        DrugEffect usages = effectDetails.stream()
                .filter(e -> e.getType() == DrugEffect.Type.USAGE)
                .toList().get(0);

        DrugEffect effects = effectDetails.stream()
                .filter(e -> e.getType() == DrugEffect.Type.EFFECT)
                .toList().get(0);

        DrugEffect cautions = effectDetails.stream()
                .filter(e -> e.getType() == DrugEffect.Type.CAUTION)
                .toList().get(0);

        DrugStorageCondition container = storageDetails.stream()
                .filter(e -> e.getCategory() == DrugStorageCondition.Category.CONTAINER)
                .toList().get(0);
        DrugStorageCondition temperature = storageDetails.stream()
                .filter(e -> e.getCategory() == DrugStorageCondition.Category.TEMPERATURE)
                .toList().get(0);

        DrugStorageCondition light = storageDetails.stream()
                .filter(e -> e.getCategory() == DrugStorageCondition.Category.LIGHT)
                .toList().get(0);

        DrugStorageCondition humid = storageDetails.stream()
                .filter(e -> e.getCategory() == DrugStorageCondition.Category.HUMID)
                .toList().get(0);

        Optional<TakingPillCounter> takingPillCounter = takingPillCounterRepository.findByDrugId(id);
        int count = takingPillCounter.map(TakingPillCounter::getCount).orElse(0);

        return drug.map(value -> DrugDetail.builder()
                .id(id)
                .name(searchDurDto.drugName())
                .manufacturer(searchDurDto.manufacturer())
                .ingredients(searchDurDto.ingredients())
                .packaging(value.getPackaging())
                .form(value.getForm())
                .tag(value.getTag())
                .atcCode(value.getAtcCode())
                .imageUrl(drug.get().getImage())
                .approvalDate(value.getApprovalDate())
                .caution(new EffectDetail(cautions.getType(),cautions.getContent()))
                .effect(new EffectDetail(effects.getType(),effects.getContent()))
                .usage(new EffectDetail(usages.getType(),usages.getContent()))
                .container(new StorageDetail(container.getCategory(), container.getValue(), container.isActive()))
                .temperature(new StorageDetail(temperature.getCategory(), temperature.getValue(), temperature.isActive()))
                .light(new StorageDetail(light.getCategory(), light.getValue(), light.isActive()))
                .humid(new StorageDetail(humid.getCategory(), humid.getValue(), humid.isActive()))
                .durTags(searchDurDto.durTags())
                .isTaking(searchDurDto.isTaking())
                .count(count)
                .build()).orElse(null);
    }

    public List<SearchIndexDTO> getDetailFromElasticsearch(long id) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s ->
                s.index(allSearch)
                        .query(q -> q
                                .term(t -> t
                                        .field(drugId)
                                        .value(id)
                                )
                        )
        );

        SearchResponse<SearchIndexDTO> response = elasticsearchClient.search(searchRequest,SearchIndexDTO.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }




}
