// author : mireutale
// description : JWT 토큰 관리를 위한 서비스 클래스
package com.oauth2.Account.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.Account.Repository.AccountTokenRepository;
import com.oauth2.Util.Exception.CustomException.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.BadCredentialsException;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * JWT 토큰 관리를 위한 서비스 클래스
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 갱신을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;

    // JWT 시크릿 키 (application.properties에서 주입)
    @Value("${jwt.secret}")
    private String secretKey;

    // 액세스 토큰 유효 시간 (분 단위)
    @Value("${jwt.access-token-validity-in-minutes}")
    private long accessTokenValidityInMinutes;

    // 리프레시 토큰 유효 시간 (일 단위)
    @Value("${jwt.refresh-token-validity-in-days}")
    private long refreshTokenValidityInDays;

    /**
     * 새로운 액세스 토큰과 리프레시 토큰을 생성
     * @param accountId 사용자 ID
     * @return 생성된 토큰 정보를 담은 UserToken 객체
     */
    public AccountToken generateTokens(Long accountId) {
        String accessToken = generateAccessToken(accountId);
        String refreshToken = generateRefreshToken(accountId);
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(refreshTokenValidityInDays);

        // 기존 토큰이 있는지 확인
        AccountToken existingToken = accountTokenRepository.findById(accountId).orElse(null);
        Account account = accountRepository.findById(accountId).orElse(null);
        if (existingToken != null) {
            // 기존 토큰이 있으면 업데이트
            existingToken.updateTokens(accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry);
            return accountTokenRepository.save(existingToken);
        } else {
            // 기존 토큰이 없으면 새로 생성
            AccountToken newToken = AccountToken.builder()
                    .accountId(accountId)
                    .account(account)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(accessTokenExpiry)
                    .refreshTokenExpiry(refreshTokenExpiry)
                    .build();
            return accountTokenRepository.save(newToken);
        }
    }

    // 토큰 갱신 결과 클래스
    public static class TokenRefreshResult {
        private final AccountToken accountToken; // 갱신된 토큰 정보
        private final boolean isRefreshTokenRenewed; // 리프레시 토큰 갱신 여부

        // 생성자
        public TokenRefreshResult(AccountToken accountToken, boolean isRefreshTokenRenewed) {
            this.accountToken = accountToken;
            this.isRefreshTokenRenewed = isRefreshTokenRenewed;
        }

        // 갱신된 토큰 정보 반환
        public AccountToken getUserToken() {
            return accountToken;
        }

        // 리프레시 토큰 갱신 여부 반환
        public boolean isRefreshTokenRenewed() {
            return isRefreshTokenRenewed;
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급
     * @param refreshToken 기존 리프레시 토큰
     * @return 갱신된 토큰 정보를 담은 UserToken 객체
     * @throws BadCredentialsException 토큰이 유효하지 않거나 찾을 수 없는 경우
     */
    public TokenRefreshResult refreshTokens(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!validateToken(refreshToken)) {
            throw new InvalidRefreshTokenDetailException();
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Claims claims = getClaimsFromToken(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        
        // 사용자 ID로 토큰 조회
        AccountToken accountToken = accountTokenRepository.findById(userId)
                .orElseThrow(TokenNotFoundException::new);

        // 리프레시 토큰 일치 여부 검증
        if (!accountToken.getRefreshToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenDetailException();
        }

        // 보안을 위해 매번 새로운 토큰 발급 (리프레시 토큰 만료 여부와 관계없이)
        AccountToken newTokens = generateTokens(userId);
        return new TokenRefreshResult(newTokens, true);
    }

    /**
     * 액세스 토큰 생성
     * @param accountId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    private String generateAccessToken(Long accountId) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject(String.valueOf(accountId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMinutes * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }   

    /**
     * 리프레시 토큰 생성
     * @param accountId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    private String generateRefreshToken(Long accountId) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject(String.valueOf(accountId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInDays * 24 * 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰의 유효성을 검증
     * @param token 검증할 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     * @throws BadCredentialsException 토큰이 만료되었거나 유효하지 않은 경우
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredDetailException();
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
    }

    /**
     * 토큰에서 Claims를 추출
     * @param token JWT 토큰
     * @return 토큰의 Claims
     * @throws BadCredentialsException 토큰이 유효하지 않은 경우
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            } catch (ExpiredJwtException e) {
                throw new TokenExpiredDetailException();
            } catch (Exception e) {
                throw new InvalidTokenException();
            }
    }

    /**
     * 토큰에서 사용자 ID를 추출
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    // UserToken 엔티티에 Access Token만 업데이트하는 메서드 추가
    public void updateAccessToken(String accessToken, LocalDateTime accessTokenExpiry) {
        accountTokenRepository.updateAccessToken(accessToken, accessTokenExpiry);
    }

    // 커스텀 JWT 토큰 생성 (userId, questionnaireId, hospitalCode, 만료초)
    public String createCustomJwtToken(Long userId, String hospitalCode, int expiresInSeconds) {
        long now = System.currentTimeMillis();
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("hospitalCode", hospitalCode)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiresInSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 만료시간이 없는 커스텀 JWT 토큰 생성
    public String createCustomJwtTokenWithoutExpiration(Long userId, String hospitalCode) {
        long now = System.currentTimeMillis();
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("hospitalCode", hospitalCode)
                .setIssuedAt(new Date(now))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 커스텀 JWT 토큰 검증 (userId 일치)
    public boolean validateCustomJwtToken(String token, String id) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            String tokenUserId = claims.getSubject();
            return id.equals(tokenUserId);
        } catch (Exception e) {
            return false;
        }
    }

    // QR URL용 JWT 토큰 검증 (userId 일치)
    public boolean validateQRJwtToken(String token, Long userId) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            Long tokenUserId = Long.parseLong(claims.getSubject());
            return userId.equals(tokenUserId);
        } catch (Exception e) {
            return false;
        }
    }

    // 친구 추가용 jwt 생성
    public String createFriendInviteToken(Long inviterId, int expiresInSeconds) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject("friend-invite")
                .claim("inviterId", inviterId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiresInSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 검증 코드
    public Long getInviterIdFromFriendToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("inviterId", Long.class);
    }
}
