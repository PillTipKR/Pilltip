package com.oauth2.Util.Elasticsearch.Controller;

import com.oauth2.Util.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Util.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Util.Elasticsearch.Service.ElasticsearchService;
import com.oauth2.Account.Dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
@RequiredArgsConstructor
public class ElasticSearchController {

    @Value("${elastic.autocomplete.index}")
    private String drugIndex;

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String ingredient;

    @Value("${elastic.supplement.autocomplete.index}")
    private String supplementIndex;

    @Value("${elastic.supplement.name}")
    private String supplementName;

    @Value("${elastic.supplement.ingredient}")
    private String supplementIngredient;

    @Value("${elastic.supplement.enterprise}")
    private String supplementEnterprise;

    //static이면 주입이 안됨!!
    @Value("${elastic.autocomplete.page}")
    private int pageSize;

    private final ElasticsearchService elasticsearchService;

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getDrugSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getDrugApiResponseResponseEntity(input, page, drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getManufacturerSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getDrugApiResponseResponseEntity(input, page, manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getDrugApiResponseResponseEntity(input, page, ingredient);
    }

    @GetMapping("/supplements")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getSupplementSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getSupplementApiResponseResponseEntity(input, page, supplementName);
    }

    @GetMapping("/enterprises")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getSupplementEnterpriseSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getSupplementApiResponseResponseEntity(input, page, supplementEnterprise);
    }

    @GetMapping("/supplementIngredients")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getSupplementIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page
    ) throws IOException {
        return getSupplementApiResponseResponseEntity(input, page, supplementIngredient);
    }

    private ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getDrugApiResponseResponseEntity(
            String input, int page, String field) throws IOException {

        return getApiResponseResponseEntity(input, page, field, drugIndex);
    }

    private ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getSupplementApiResponseResponseEntity(
            String input, int page, String field) throws IOException {

        return getApiResponseResponseEntity(input, page, field, supplementIndex);
    }

    private ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getApiResponseResponseEntity(String input, int page, String field, String supplementIndex) throws IOException {
        List<String> filter = List.of();
        ElasticQuery eq = new ElasticQuery(input, field, supplementIndex, filter, pageSize, page);
        List<ElasticsearchDTO> result = elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
