package com.oauth2.Util.Elasticsearch.Config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${elastic.id}")
    private String id;

    @Value("${elastic.pw}")
    private String pw;

    @Value("${elastic.ip}")
    private String ip;

    @Value("${elastic.port}")
    private String port;


    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
            .connectedTo(ip+":"+port)
            .withBasicAuth(id, pw)
            .build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(id, pw)); //아이디 비번

        RestClientBuilder builder = RestClient.builder(
                        new org.apache.http.HttpHost(ip, Integer.parseInt(port)))  // 도커 컨테이너의 주소
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );

        RestClient restClient = builder.build();

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
    

    /**
     * 인덱스 생성 및 자소 분석기 설정은 별도 스크립트나 API 호출로 구현
     * 
     * PUT drug_autocomplete
     * {
     *   "settings": {
     *     "analysis": {
     *       "analyzer": {
     *         "edgeNgram_analyzer": {
     *           "type": "custom",
     *           "tokenizer": "edgeNgram_tokenizer"
     *         }
     *       },
     *       "tokenizer": {
     *         "edgeNgram_tokenizer": {
     *           "type": "edgeNgram_tokenizer"
     *         }
     *       }
     *     }
     *   },
     *   "mappings": {
     *     "properties": {
     *       "drugId": { "type": "keyword" },
     *       "productName": { "type": "text", "analyzer": "edgeNgram_analyzer" },
     *       "manufacturer": { "type": "text", "analyzer": "edgeNgram_analyzer" }
     *     }
     *   }
     * }
     */
} 