
package com.oauth2.HealthSupplement.Search.Provider;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import com.oauth2.Util.Elasticsearch.Provider.CommonSettingsProvider;
import com.oauth2.Util.Elasticsearch.Provider.IndexMappingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SupplementSearchProvider implements IndexMappingProvider<IngredientDetail> {

    @Value("${elastic.supplement.search}")
    private String index;
    @Value("${elastic.supplement.id}")
    private String id;
    @Value("${elastic.supplement.ingredient.index}")
    private String ingredient;
    @Value("${elastic.supplement.name}")
    private String supplementName;
    @Value("${elastic.supplement.enterprise}")
    private String enterprise;


    private final CommonSettingsProvider settingsProvider;

    public SupplementSearchProvider(CommonSettingsProvider settingsProvider) {
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
                .properties(supplementName, p -> p
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
                .properties(enterprise, p -> p
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

