package com.oauth2.HealthSupplement.Search.Service;

import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.HealthSupplement.Search.Dto.SupplementSearchIndexDto;
import com.oauth2.Util.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Util.Elasticsearch.Service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplementSearchService {

    @Value("${elastic.supplement.search}")
    private String index;

    private final ElasticsearchService elasticsearchService;

    public List<SupplementSearchIndexDto> getSupplementSearch(String input, String field,
                                                              int pageSize, int page) throws IOException {
        List<String> source = List.of();
        ElasticQuery eq = new ElasticQuery(input,field, index,source,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, SupplementSearchIndexDto.class);

    }

}
