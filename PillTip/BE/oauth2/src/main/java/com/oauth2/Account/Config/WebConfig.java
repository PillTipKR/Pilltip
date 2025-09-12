// author : mireutale
// description : 앱, 웹 설정 / CORS
package com.oauth2.Account.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 URL 패턴에 대해 CORS 허용
                .allowedOriginPatterns("*")  // 모든 도메인에서의 요청 허용 (Spring Boot 2.4 이상에서 권장)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드 지정
                .allowedHeaders("*") // 모든 요청 헤더 허용
                .allowCredentials(true) // 인증 정보(쿠키, Authorization 헤더 등)를 클라이언트에서 전송 가능
                .maxAge(3600); // CORS 사전 요청(pre-flight)의 캐시 시간을 초 단위로 설정 (1시간)
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
