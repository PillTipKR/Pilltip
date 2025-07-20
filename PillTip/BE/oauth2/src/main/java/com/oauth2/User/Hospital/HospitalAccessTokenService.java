package com.oauth2.User.Hospital;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HospitalAccessTokenService {
    
    private final HospitalAccessTokenRepository tokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 병원별 일일 접근 토큰 생성
     */
    @Transactional
    public String generateDailyToken(String hospitalCode) {
        try {
            System.out.println("=== 토큰 생성 시작 ===");
            System.out.println("hospitalCode: " + hospitalCode);
            
            LocalDate today = LocalDate.now();
            System.out.println("오늘 날짜: " + today);
            
            // 기존 토큰 조회
            Optional<HospitalAccessToken> existingTokenOpt = tokenRepository.findByHospitalCode(hospitalCode);
            
            // 새로운 토큰 생성 (32바이트 랜덤)
            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            String accessToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            System.out.println("새 토큰 생성: " + accessToken.substring(0, 8) + "...");
            
            HospitalAccessToken savedToken;
            
            if (existingTokenOpt.isPresent()) {
                // 기존 토큰 업데이트
                HospitalAccessToken existingToken = existingTokenOpt.get();
                existingToken.setAccessToken(accessToken);
                existingToken.setTokenDate(today);
                existingToken.setCreatedAt(LocalDateTime.now());
                existingToken.setExpiresAt(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
                
                savedToken = tokenRepository.save(existingToken);
                System.out.println("기존 토큰 업데이트 완료, ID: " + savedToken.getId());
            } else {
                // 새 토큰 생성
                HospitalAccessToken token = HospitalAccessToken.builder()
                    .hospitalCode(hospitalCode)
                    .accessToken(accessToken)
                    .tokenDate(today)
                    .build();
                
                savedToken = tokenRepository.save(token);
                System.out.println("새 토큰 생성 완료, ID: " + savedToken.getId());
            }
            
            return accessToken;
        } catch (Exception e) {
            System.out.println("=== 토큰 생성 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String accessToken) {
        Optional<HospitalAccessToken> tokenOpt = tokenRepository.findByAccessToken(accessToken);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        HospitalAccessToken token = tokenOpt.get();
        LocalDate today = LocalDate.now();
        
        // 토큰 날짜가 오늘이 아니거나 만료되었으면 무효
        if (!token.getTokenDate().equals(today) || 
            token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 토큰으로 병원 코드 조회
     */
    public Optional<String> getHospitalCodeByToken(String accessToken) {
        return tokenRepository.findByAccessToken(accessToken)
            .map(HospitalAccessToken::getHospitalCode);
    }
    
    /**
     * 병원 코드로 현재 토큰 조회
     */
    public Optional<String> getCurrentTokenByHospitalCode(String hospitalCode) {
        try {
            System.out.println("=== 토큰 조회 시작 ===");
            System.out.println("hospitalCode: " + hospitalCode);
            System.out.println("오늘 날짜: " + LocalDate.now());
            
            var tokenOpt = tokenRepository.findByHospitalCodeAndTokenDate(hospitalCode, LocalDate.now());
            
            if (tokenOpt.isPresent()) {
                String token = tokenOpt.get().getAccessToken();
                System.out.println("토큰 찾음: " + token.substring(0, 8) + "...");
                return Optional.of(token);
            } else {
                System.out.println("토큰을 찾을 수 없음");
                return Optional.empty();
            }
        } catch (Exception e) {
            System.out.println("=== 토큰 조회 실패 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 만료된 토큰 정리
     */
    @Transactional
    public void cleanupExpiredTokens() {
        // 만료된 토큰들을 삭제하는 로직
        // JPA의 @Scheduled와 함께 사용하여 주기적으로 정리
    }

    public void deleteTokenByHospitalCode(String hospitalCode) {
        tokenRepository.deleteByHospitalCode(hospitalCode);
    }
} 