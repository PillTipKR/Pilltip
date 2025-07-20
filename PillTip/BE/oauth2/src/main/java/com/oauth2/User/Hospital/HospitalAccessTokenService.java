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
        LocalDate today = LocalDate.now();
        
        // 기존 토큰이 있으면 삭제
        tokenRepository.deleteByHospitalCode(hospitalCode);
        
        // 새로운 토큰 생성 (32바이트 랜덤)
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String accessToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // 토큰 저장
        HospitalAccessToken token = HospitalAccessToken.builder()
            .hospitalCode(hospitalCode)
            .accessToken(accessToken)
            .tokenDate(today)
            .build();
        
        tokenRepository.save(token);
        
        return accessToken;
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
        return tokenRepository.findByHospitalCodeAndTokenDate(hospitalCode, LocalDate.now())
            .map(HospitalAccessToken::getAccessToken);
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