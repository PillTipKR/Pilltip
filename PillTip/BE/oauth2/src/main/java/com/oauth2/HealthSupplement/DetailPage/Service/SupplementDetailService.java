package com.oauth2.HealthSupplement.DetailPage.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.HealthSupplement.DUR.Dto.SupplementSearchDurDto;
import com.oauth2.HealthSupplement.DUR.Service.SupplementDurTaggingService;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementDetail;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementEffectDetail;
import com.oauth2.HealthSupplement.DetailPage.Dto.SupplementStorageDetail;
import com.oauth2.HealthSupplement.Search.Dto.SupplementSearchIndexDto;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementEffect;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementStorageCondition;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.User.TakingPill.Entity.TakingPillCounter;
import com.oauth2.User.TakingPill.Repositoty.TakingPillCounterRepository;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplementDetailService {

    private final HealthSupplementRepository healthSupplementRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final SupplementDurTaggingService supplementDurTaggingService;
    private final TakingPillCounterRepository takingPillCounterRepository;

    @Value("${elastic.supplement.id}")
    private String supplementId;

    @Value("${elastic.supplement.search}")
    private String supplementSearch;

    public SupplementDetail getDetail(User user, Long id) throws IOException {
        SupplementSearchDurDto supplementSearchDurDto = supplementDurTaggingService.generateTagsForSupplements(user, getDetailFromElasticsearch(id)).get(0);

        Optional<HealthSupplement> supplement = healthSupplementRepository.findSuppelmentWithAllRelations(id);
        Set<HealthSupplementEffect> effectDetails = new HashSet<>();
        Set<HealthSupplementStorageCondition> storageDetails = new HashSet<>();
        if(supplement.isPresent()){
            effectDetails = supplement.get().getSupplementEffects();
            storageDetails = supplement.get().getStorageConditions();
        }
        HealthSupplementEffect usages = effectDetails.stream()
                .filter(e -> e.getType() == HealthSupplementEffect.Type.USAGE)
                .toList().get(0);

        HealthSupplementEffect effects = effectDetails.stream()
                .filter(e -> e.getType() == HealthSupplementEffect.Type.EFFECT)
                .toList().get(0);

        HealthSupplementEffect cautions = effectDetails.stream()
                .filter(e -> e.getType() == HealthSupplementEffect.Type.CAUTION)
                .toList().get(0);

        HealthSupplementStorageCondition fridge = storageDetails.stream()
                .filter(e -> e.getCategory() == HealthSupplementStorageCondition.Category.FRIDGE)
                .toList().get(0);
        HealthSupplementStorageCondition temperature = storageDetails.stream()
                .filter(e -> e.getCategory() == HealthSupplementStorageCondition.Category.TEMPERATURE)
                .toList().get(0);

        HealthSupplementStorageCondition light = storageDetails.stream()
                .filter(e -> e.getCategory() == HealthSupplementStorageCondition.Category.LIGHT)
                .toList().get(0);

        HealthSupplementStorageCondition humid = storageDetails.stream()
                .filter(e -> e.getCategory() == HealthSupplementStorageCondition.Category.HUMID)
                .toList().get(0);

        // 건기식 정보로 바꾸기
        Optional<TakingPillCounter> takingPillCounter = takingPillCounterRepository.findByDrugId(id);
        int count = takingPillCounter.map(TakingPillCounter::getCount).orElse(0);

        return supplement.map(value -> SupplementDetail.builder()
                .id(id)
                .name(supplementSearchDurDto.supplementName())
                .enterprise(supplementSearchDurDto.enterprise())
                .ingredients(supplementSearchDurDto.ingredients())
                .form(value.getForm())
                .dispos(value.getDispos())
                .validTerm(value.getValidTerm())
                .imageUrl("")
                .caution(new SupplementEffectDetail(cautions.getType(),cautions.getContent()))
                .effect(new SupplementEffectDetail(effects.getType(),effects.getContent()))
                .usage(new SupplementEffectDetail(usages.getType(),usages.getContent()))
                .fridge(new SupplementStorageDetail(fridge.getCategory(), fridge.getValue(), fridge.isActive()))
                .temperature(new SupplementStorageDetail(temperature.getCategory(), temperature.getValue(), temperature.isActive()))
                .light(new SupplementStorageDetail(light.getCategory(), light.getValue(), light.isActive()))
                .humid(new SupplementStorageDetail(humid.getCategory(), humid.getValue(), humid.isActive()))
                .durTags(supplementSearchDurDto.durTags())
                .isTaking(supplementSearchDurDto.isTaking())
                .count(count)
                .build()).orElse(null);
    }

    public List<SupplementSearchIndexDto> getDetailFromElasticsearch(long id) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s ->
                s.index(supplementSearch)
                        .query(q -> q
                                .term(t -> t
                                        .field(supplementId)
                                        .value(id)
                                )
                        )
        );

        SearchResponse<SupplementSearchIndexDto> response = elasticsearchClient.search(searchRequest,SupplementSearchIndexDto.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }




}
