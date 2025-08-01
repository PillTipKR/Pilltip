package com.oauth2.Util.Seeder;

import com.oauth2.Util.Elasticsearch.Service.DataSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class ElasticSeeder implements CommandLineRunner {

    //ElasticSearch 재생성 스위치
    @Value("${elastic.drug.seed}")
    private boolean drugSeed;

    @Value("${elastic.supplement.seed}")
    private boolean supplementSeed;

    private final DataSyncService dataSyncService;
    @Override
    public void run(String... args) throws Exception {
        if(drugSeed) {
            dataSyncService.loadDrug();
            System.out.println("Drug Index injection complete");
        }
        if(supplementSeed) {
            dataSyncService.loadSupplement();
            System.out.println("Supplement Index injection complete");
        }
    }
}
