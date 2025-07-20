package com.oauth2.User.Hospital;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HospitalTokenScheduler {
    
    private final HospitalRepository hospitalRepository;
    private final HospitalAccessTokenService tokenService;
    
    /**
     * 매일 자정(00:00)에 모든 병원의 접근 토큰을 갱신
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    @Transactional
    public void generateDailyTokens() {
        log.info("=== 병원별 일일 접근 토큰 갱신 시작 ===");
        log.info("현재 시간: {}", java.time.LocalDateTime.now());
        
        try {
            List<Hospital> hospitals = hospitalRepository.findAll();
            log.info("총 {}개 병원 발견", hospitals.size());
            
            for (Hospital hospital : hospitals) {
                log.info("병원 {} 토큰 갱신 중...", hospital.getName());
                String newToken = tokenService.generateDailyToken(hospital.getHospitalCode());
                log.info("병원 {} 토큰 갱신 완료: {}", hospital.getName(), newToken.substring(0, 8) + "...");
            }
            
            log.info("=== 병원별 일일 접근 토큰 갱신 완료. 총 {}개 병원 ===", hospitals.size());
            
        } catch (Exception e) {
            log.error("병원별 일일 접근 토큰 갱신 중 오류 발생", e);
        }
    }
    
    /**
     * 매일 오전 1시에 만료된 토큰 정리
     */
    @Scheduled(cron = "0 0 1 * * ?") // 매일 오전 1시
    public void cleanupExpiredTokens() {
        log.info("만료된 병원 접근 토큰 정리 시작");
        
        try {
            tokenService.cleanupExpiredTokens();
            log.info("만료된 병원 접근 토큰 정리 완료");
            
        } catch (Exception e) {
            log.error("만료된 병원 접근 토큰 정리 중 오류 발생", e);
        }
    }
} 