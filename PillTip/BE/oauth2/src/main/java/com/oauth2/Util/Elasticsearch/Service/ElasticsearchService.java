package com.oauth2.Util.Elasticsearch.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.Util.Elasticsearch.Dto.ElasticQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ElasticsearchService {


    @Value("${elastic.autocomplete.index}")
    private String drugAutocomplete;

    @Value("${elastic.supplement.autocomplete.index}")
    private String supplementAutocomplete;

    @Value("${elastic.autocomplete.field}")
    private String drugAutocompleteField;

    @Value("${elastic.supplement.autocomplete.field}")
    private String supplementAutocompleteField;

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    // 인덱스를 기준으로 analyzer,tokenizer,indexsetting 등을 가짐
    // 즉, 성분, 제조사 등의 DTO가 다르다면, 다른 타입이 매핑되어야 하므로, 새 인덱스를 생성해야할듯
    // 성분, 제조사는 약과 같이 이미지가 없으므로, 다른 dto를 사용할 가능성이 높음 / 혹은 프론트 단에서 무시
    // 이미지는 어떻게 넣을것?
    //- firebase에 약품 id와 매핑해서 저장 후, 약의 대한 정보를 주면 firebase에서 받아오기
    //- 이미지 url도 DB에 저장해서 사용하기


    private NestedQuery nestedQuery(String field, String value) {
        String nestedPath = field.split("\\.")[0]; // e.g., "ingredient"
        return NestedQuery.of(q -> q
                .path(nestedPath)
                .query(nq -> nq
                        .bool(b -> b
                                .must(mq -> mq
                                        .match(m -> m
                                                .field(field)
                                                .query(value)
                                        )
                                )
                        )
                )
        );
    }



    public  <T> List<T> getMatchingFromElasticsearch(ElasticQuery eq, Class<T> dtoClass) throws IOException {
        String input = eq.getInput();
        final String[] field = {eq.getField()};
        String index = eq.getIndex();
        int pageSize = eq.getPageSize();
        int from = eq.getPage() * pageSize;
        SearchRequest searchRequest = SearchRequest.of(s -> {
            s = s.index(index)
                    .from(from)
                    .size(pageSize);

            if (eq.getSources() != null) {
                if (!eq.getSources().isEmpty()) {
                    s = s.source(src -> src.filter(f -> f.includes(eq.getSources())));
                } else {
                    s = s.source(src -> src.filter(f -> f.includes("*"))); // or 생략
                }
            }

            s = s.query(q -> q
                    .bool(b -> {
                            if(index.equals(drugAutocomplete)) {
                                b = b.must(m -> m.term(t -> t.field("type").value(field[0])));
                                field[0] = drugAutocompleteField;
                            }
                            if(index.equals(supplementAutocomplete)) {
                                b = b.must(m -> m.term(t -> t.field("type").value(field[0])));
                                field[0] = supplementAutocompleteField;
                            }

                            b.minimumShouldMatch("1");

                        if (field[0].startsWith("ingredient.")) {
                            b = b
                                .should(sh -> sh.nested(nestedQuery(field[0] + ".edge", input)))
                                .should(sh -> sh.nested(nestedQuery(field[0] + ".gram", input)));
                        }
                        else {
                                b=b.should(sh -> sh.prefix(p -> p
                                    .field(field[0] + ".edge")
                                    .value(input)
                                    .boost(90.0f)
                                ))
                                .should(sh -> sh.match(p -> p
                                    .field(field[0] + ".gram")
                                    .query(input)
                                    .boost(5.0f)
                                ))
                                .should(sh -> sh.fuzzy(f -> f
                                    .field(field[0])
                                    .value(input)
                                    .fuzziness("AUTO")
                                    .boost(1.0f)
                                ));
                        }

                        return b;
                    })
            );
            return s;
        });
        SearchResponse<T> response = elasticsearchClient.search(searchRequest,dtoClass);
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}
