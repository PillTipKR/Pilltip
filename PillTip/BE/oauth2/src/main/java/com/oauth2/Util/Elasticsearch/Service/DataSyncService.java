package com.oauth2.Util.Elasticsearch.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugIngredient;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.Drug.Search.Dto.IngredientComp;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.HealthSupplement.Search.Dto.SupplementSearchIndexDto;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthIngredient;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplement;
import com.oauth2.HealthSupplement.SupplementInfo.Entity.HealthSupplementIngredient;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthIngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementIngredientRepository;
import com.oauth2.HealthSupplement.SupplementInfo.Repository.HealthSupplementRepository;
import com.oauth2.Util.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Util.Elasticsearch.Dto.SupplementIngrComp;
import com.oauth2.Util.Elasticsearch.Dto.SupplementIngrDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataSyncService {

    @Value("${elastic.autocomplete.index}")
    private String drugAutocomplete;
    @Value("${elastic.allSearch}")
    private String drugSearch;

    @Value("${elastic.drug.drug}")
    private String drugName;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String drugIngredientName;

    @Value("${elastic.supplement.ingredient}")
    private String supplementIngredientName;

    @Value("${elastic.supplement.autocomplete.index}")
    private String supplementAutocomplete;

    @Value("${elastic.supplement.search}")
    private String supplementSearch;

    @Value("${elastic.supplement.name}")
    private String supplementName;

    @Value("${elastic.supplement.enterprise}")
    private String supplementEnterprise;

    private final DrugRepository drugRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final IngredientRepository ingredientRepository;
    private final DrugIngredientRepository pillIngredientRepository;
    private final HealthSupplementRepository healthSupplementRepository;
    private final HealthIngredientRepository healthIngredientRepository;
    private final HealthSupplementIngredientRepository healthSupplementIngredientRepository;

    private void syncTextToElasticsearch() throws IOException {
        List<Drug> pills = drugRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Set<String> manufacturers = new HashSet<>();
        for (Drug pill : pills){
            injectIndex(drugName, pill.getId(), pill.getName(), pill.getImage(), drugAutocomplete);
            if (manufacturers.add(pill.getManufacturer())) {
                injectIndex(manufacturer, 0L, pill.getManufacturer(), null, drugAutocomplete);
            }
        }
        for(Ingredient ingredient : ingredients){
            injectIndex(drugIngredientName,ingredient.getId(),ingredient.getNameKr(),null, drugAutocomplete);
        }
        System.out.println("index injection completed");
    }

    private void syncSupplmentTextToElasticsearch() throws IOException {
        List<HealthSupplement> supplements = healthSupplementRepository.findAll();
        List<HealthIngredient> ingredients = healthIngredientRepository.findAll();
        Set<String> enterprise = new HashSet<>();
        for (HealthSupplement healthSupplement : supplements){
            injectIndex(supplementName, healthSupplement.getId(), healthSupplement.getProductName(), null, supplementAutocomplete);
            if (enterprise.add(healthSupplement.getEnterprise())) {
                injectIndex(supplementEnterprise, 0L, healthSupplement.getEnterprise(), null, supplementAutocomplete);
            }
        }
        for(HealthIngredient ingredient: ingredients){
            injectIndex(supplementIngredientName,ingredient.getId(),ingredient.getName(),null, supplementAutocomplete);
        }
        System.out.println("index injection completed");
    }


    private void injectIndex(String type,Long id, String value, String imageUrl, String index) throws IOException {
        // 중복 방지를 위한 고유 ID 생성
        ElasticsearchDTO elasticsearchDTO = new ElasticsearchDTO(
                type,
                id,
                value,
                imageUrl
        );

        IndexRequest<ElasticsearchDTO> indexRequest = new IndexRequest.Builder<ElasticsearchDTO>()
                .index(index)
                .document(elasticsearchDTO)
                .build();

        elasticsearchClient.index(indexRequest);
    }


    private void syncDrugsToElasticsearch() throws IOException {
        List<Drug> pills = drugRepository.findAll();
        for (Drug pill : pills) {
            List<DrugIngredient> di = pillIngredientRepository.findById_DrugId(pill.getId());
            List<IngredientComp> ingredientComps = new ArrayList<>();
            for(DrugIngredient ding : di){
                Optional<Ingredient> ing =
                        ingredientRepository.findById(ding.getId().getIngredientId());
                if(ing.isPresent()){
                    IngredientComp isidto = new IngredientComp(
                            ing.get().getNameKr(),
                            ding.getAmount() !=null? ding.getAmount():0,
                            ding.getAmountBackup()+ding.getUnit(),
                            false
                    );
                    ingredientComps.add(isidto);
                }
            }
            if(!ingredientComps.isEmpty()) {
                ingredientComps.sort(Collections.reverseOrder());
                ingredientComps.get(0).setMain(true);
                SearchIndexDTO dto = getSearchIndexDTO(pill, ingredientComps);

                IndexRequest<SearchIndexDTO> indexRequest = new IndexRequest.Builder<SearchIndexDTO>()
                        .index(drugSearch)
                        .id(String.valueOf(dto.id()))
                        .document(dto)
                        .build();

                elasticsearchClient.index(indexRequest);
            }
        }
    }

    private static SearchIndexDTO getSearchIndexDTO(Drug pill, List<IngredientComp> ingredientComps) {
        List<IngredientDetail> ingredientDetails = new ArrayList<>();
        for(IngredientComp ingredientComp : ingredientComps){
            IngredientDetail ingredientDetail = new IngredientDetail(
                    ingredientComp.getName(),
                    ingredientComp.getBackup(),
                    ingredientComp.isMain());
            ingredientDetails.add(ingredientDetail);
        }
        return new SearchIndexDTO(
                pill.getId(),
                pill.getName(),
                ingredientDetails,
                pill.getManufacturer()
        );
    }


    private void syncSupplementsToElasticsearch() throws IOException {
        List<HealthSupplement> supplements = healthSupplementRepository.findAll();
        for (HealthSupplement supplement : supplements) {
            List<HealthSupplementIngredient> hsis = healthSupplementIngredientRepository.findBySupplementId(supplement.getId());
            List<SupplementIngrComp> ingredientComps = new ArrayList<>();
            for(HealthSupplementIngredient hsi : hsis){
                HealthIngredient ing = hsi.getIngredient();
                SupplementIngrComp isidto = new SupplementIngrComp(
                        ing.getName(),
                        hsi.getAmount() !=null? hsi.getAmount():0,
                        hsi.getUnit(),
                        false
                );
                ingredientComps.add(isidto);
            }
            if(!ingredientComps.isEmpty()) {
                ingredientComps.sort(Collections.reverseOrder());
                ingredientComps.get(0).setMain(true);
                SupplementSearchIndexDto dto = getSupplementSearchIndexDTO(supplement, ingredientComps);

                IndexRequest<SupplementSearchIndexDto> indexRequest = new IndexRequest.Builder<SupplementSearchIndexDto>()
                        .index(supplementSearch)
                        .id(String.valueOf(dto.id()))
                        .document(dto)
                        .build();

                elasticsearchClient.index(indexRequest);
            }
        }
    }

    private static SupplementSearchIndexDto getSupplementSearchIndexDTO(HealthSupplement supplement, List<SupplementIngrComp> ingredientComps) {
        List<SupplementIngrDetail> ingredientDetails = new ArrayList<>();
        for(SupplementIngrComp ingredientComp : ingredientComps){
            SupplementIngrDetail ingredientDetail = new SupplementIngrDetail(
                    ingredientComp.getName(),
                    ingredientComp.getDose(),
                    ingredientComp.getUnit(),
                    ingredientComp.isMain());
            ingredientDetails.add(ingredientDetail);
        }
        return new SupplementSearchIndexDto(
                supplement.getId(),
                supplement.getProductName(),
                ingredientDetails,
                supplement.getEnterprise()
        );
    }

    public void loadDrug() throws IOException {
        syncDrugsToElasticsearch();
        syncTextToElasticsearch();
    }

    public void loadSupplement() throws IOException {
        syncSupplementsToElasticsearch();
        syncSupplmentTextToElasticsearch();
    }
}
