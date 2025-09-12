package com.oauth2.Util.Elasticsearch.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.Util.Elasticsearch.Dto.ElasticsearchDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchProvider implements IndexMappingProvider<ElasticsearchDTO> {

    private final CommonSettingsProvider settingsProvider;

    public ElasticsearchProvider(CommonSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Value("${elastic.autocomplete.index}")
    private String index;

    @Value("${elastic.autocomplete.field}")
    private String field;

    @Override
    public String getIndexName() {
        return index;
    }

    @Override
    public Class<ElasticsearchDTO> getDtoClass() {
        return ElasticsearchDTO.class;
    }

    @Override
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
                .properties("type", p -> p.keyword(k -> k)) // drugName, ingredient 등
                .properties("id", p -> p.keyword(k -> k.index(false)))
                .properties(field, p -> p
                        .text(t -> t
                                .fields("edge", f -> f.text(edge -> edge.analyzer(settingsProvider.getAutoEdgeNGramAnalyzer())))
                                .fields("gram", f -> f.text(gram -> gram.analyzer(settingsProvider.getAutoNGramAnalyzer())))
                        )
                )
                .properties("image", p -> p.keyword(k -> k.index(false)))
                .build();
    }
    @Override
    public IndexSettings getSettings() {
        return settingsProvider.getDefaultSettings();
    }
}
