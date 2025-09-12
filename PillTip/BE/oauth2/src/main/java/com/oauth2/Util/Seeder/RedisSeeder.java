package com.oauth2.Util.Seeder;

import com.oauth2.Util.Redis.DurRedisLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class RedisSeeder implements CommandLineRunner {

    //redis 재생성 스위치
    @Value("${redis.seed}")
    private boolean seed;

    private final DurRedisLoader durRedisLoader;

    @Override
    public void run(String... args) throws Exception {
        if(seed) {
            durRedisLoader.loadAll();
            System.out.println("DUR Redis injection complete");
        }
    }
}
