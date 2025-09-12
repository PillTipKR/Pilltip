package com.oauth2.Util.Elasticsearch.Dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ElasticQuery {
    String input;
    String field;
    String index;
    List<String> sources;
    int pageSize;
    int page;
}
