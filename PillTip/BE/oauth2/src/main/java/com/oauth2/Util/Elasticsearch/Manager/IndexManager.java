package com.oauth2.Util.Elasticsearch.Manager;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.oauth2.Util.Elasticsearch.Provider.IndexMappingProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IndexManager {

    private final ElasticsearchClient client;

    public IndexManager(ElasticsearchClient client) {
        this.client = client;
    }

    public <T> void createIndex(IndexMappingProvider<T> provider) throws IOException {
        String index = provider.getIndexName();
        if (client.indices().exists(e -> e.index(index)).value()) {
            client.indices().delete(d -> d.index(index));
        }

        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(index)
                .settings(provider.getSettings())
                .mappings(provider.getMapping())
                .build();

        client.indices().create(request);
    }
}
