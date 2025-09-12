package com.oauth2.Drug.Search.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.Util.Elasticsearch.Provider.CommonSettingsProvider;
import com.oauth2.Util.Elasticsearch.Provider.IndexMappingProvider;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DrugSearchProvider implements IndexMappingProvider<IngredientDetail> {

    @Value("${elastic.allSearch}")
    private String index;
    @Value("${elastic.drug.id}")
    private String id;
    @Value("${elastic.drug.ingredient.index}")
    private String ingredient;
    @Value("${elastic.drug.drug}")
    private String drugName;
    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;


    private final CommonSettingsProvider settingsProvider;

    public DrugSearchProvider(CommonSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    public String getIndexName() {
        return index;
    }

    @Override
    public Class<IngredientDetail> getDtoClass() {
        return IngredientDetail.class;
    }

    @Override
    public TypeMapping getMapping() {
        return new TypeMapping.Builder()
                .properties(id, p -> p
                        .keyword(k -> k.index(true))
                )
                .properties(drugName, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(settingsProvider.getAutoEdgeNGramAnalyzer())))
                                .fields("gram", f->f.text(gram->gram.analyzer(settingsProvider.getAutoNGramAnalyzer()))))
                )
                .properties(ingredient, p -> p
                        .nested(n -> n
                                .properties("name", np -> np
                                        .text(t -> t
                                                .fields("edge", f -> f.text(edge -> edge.analyzer(settingsProvider.getAutoEdgeNGramAnalyzer())))
                                                .fields("gram", f -> f.text(gram -> gram.analyzer(settingsProvider.getAutoNGramAnalyzer())))
                                        )
                                )
                                .properties("dose", d -> d.text(t->t))
                                .properties("is_main", np -> np.boolean_(b -> b))
                                .properties("priority", np -> np.integer(i -> i))
                        )
                )
                .properties(manufacturer, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(settingsProvider.getAutoEdgeNGramAnalyzer())))
                                .fields("gram", f->f.text(gram->gram.analyzer(settingsProvider.getAutoNGramAnalyzer()))))
                )
                .build();
    }
    @Override
    public IndexSettings getSettings() {
        return settingsProvider.getDefaultSettings();
    }
}

