package com.oauth2.Util.Elasticsearch.Initializer;

import com.oauth2.Util.Elasticsearch.Manager.IndexManager;
import com.oauth2.Util.Elasticsearch.Provider.IndexMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class IndexInitializer implements CommandLineRunner {

    //ElasticSearch 재생성 스위치
    @Value("${elastic.drug.seed}")
    private boolean drugSeed;

    @Value("${elastic.allSearch}")
    private String drugSearch;

    @Value("${elastic.autocomplete.index}")
    private String autocompleteIndex;

    @Value("${elastic.supplement.seed}")
    private boolean supplementSeed;

    private final IndexManager indexManager;
    private final List<IndexMappingProvider<?>> indexProviders;

    public IndexInitializer(IndexManager indexManager, List<IndexMappingProvider<?>> indexProviders) {
        this.indexManager = indexManager;
        this.indexProviders = indexProviders;
    }

    @Override
    public void run(String... args) throws Exception {
        for (IndexMappingProvider<?> provider : indexProviders) {
            if(provider.getIndexName().equals(autocompleteIndex)
                    || provider.getIndexName().equals(drugSearch)) {
                if(drugSeed) {
                    indexManager.createIndex(provider);
                    System.out.println("Creating index in ES:" + provider.getIndexName());
                }
            }else{
                if(supplementSeed) {
                    indexManager.createIndex(provider);
                    System.out.println("Creating index in ES:" + provider.getIndexName());
                }
            }

        }
    }
}

