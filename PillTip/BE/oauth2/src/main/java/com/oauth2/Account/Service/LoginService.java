package com.oauth2.Account.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.Account.Dto.LoginRequest;
import com.oauth2.Account.Dto.LoginResponse;
import com.oauth2.Account.Dto.OAuth2UserInfo;
import com.oauth2.Account.Dto.SocialLoginRequest;
import com.oauth2.Account.Entity.LoginType;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Util.Exception.CustomException.InvalidRequestException;
import com.oauth2.Util.Exception.CustomException.UserInfoNotFoundRetryLoginException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;
    private final Logger logger = LoggerFactory.getLogger(LoginService.class);

    // ID/PW 로그인
    public LoginResponse login(LoginRequest request) {
        logger.info("ID/PW 로그인 시도 - LoginId: {}", request.loginId());
        
        // 모든 사용자를 조회하여 loginId를 비교 (EncryptionConverter가 자동으로 복호화)
        List<Account> allAccounts = accountRepository.findAll();

        Account account = null;
        for(Account a : allAccounts) {
            if(a.getLoginId() != null && a.getLoginId().equals(request.loginId())) {
                account = a;
                break;
            }
        }

        
        if (account == null) {
            logger.error("로그인 실패: loginId {}로 사용자를 찾을 수 없습니다", request.loginId());
            throw new UserInfoNotFoundRetryLoginException();
        }

        if (account.getLoginType() != LoginType.IDPW) {
            logger.error("로그인 실패: 사용자 {}의 로그인 타입이 잘못되었습니다: {}", account.getId(), account.getLoginType());
            throw new InvalidRequestException();
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            logger.error("로그인 실패: 사용자 {}의 비밀번호가 일치하지 않습니다", account.getId());
            throw new InvalidRequestException();
        }

        AccountToken accountToken = tokenService.generateTokens(account.getId());
        updateFCMToken(account);
        return new LoginResponse(
                    accountToken.getAccessToken(),
                    accountToken.getRefreshToken()
                );
    }

    // 소셜 로그인
    public LoginResponse socialLogin(SocialLoginRequest request) {
        logger.info("소셜 로그인 시도 - Provider: {}, Token: {}", 
                   request.getProvider(), request.getToken() != null ? request.getToken().substring(0, Math.min(10, request.getToken().length())) + "..." : "null");
        
        try {
            // OAuth2 서버에서 사용자 정보 가져오기
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );

            logger.info("OAuth2 사용자 정보 조회 완료 - SocialId: {}", oauth2UserInfo.getSocialId());

            // 모든 사용자를 조회하여 socialId를 비교
            List<Account> allAccounts = accountRepository.findAll();

            Account account = null;
            for(Account a : allAccounts) {
                if(a.getSocialId() != null && a.getSocialId().equals(oauth2UserInfo.getSocialId())) {
                    account = a;
                    break;
                }
            }
            
            if (account == null) {
                logger.error("소셜 로그인 실패: socialId {}로 사용자를 찾을 수 없습니다", oauth2UserInfo.getSocialId());
                throw new UserInfoNotFoundRetryLoginException();
            }

            if (account.getLoginType() != LoginType.SOCIAL) {
                logger.error("소셜 로그인 실패: 사용자 {}의 로그인 타입이 잘못되었습니다: {}", account.getId(), account.getLoginType());
                throw new InvalidRequestException();
            }

            AccountToken accountToken = tokenService.generateTokens(account.getId());
            updateFCMToken(account);

            return new LoginResponse(
                    accountToken.getAccessToken(),
                    accountToken.getRefreshToken()
            );
        } catch (Exception e) {
            logger.error("소셜 로그인 실패: {}", e.getMessage());
            throw e;
        }
    }

    // 토큰 갱신
    public LoginResponse refreshToken(String refreshToken) {
        TokenService.TokenRefreshResult result = tokenService.refreshTokens(refreshToken);
        AccountToken accountToken = result.getUserToken();
        Account account = accountRepository.findById(accountToken.getAccountId())
                .orElseThrow(UserInfoNotFoundRetryLoginException::new);
        updateFCMToken(account);
        return new LoginResponse(
                accountToken.getAccessToken(),
                accountToken.getRefreshToken()
        );
    }

    private void updateFCMToken(Account account) {
        if(account.getFCMToken() == null) {
            // FCM 토큰이 없으면 새로 생성
            FCMToken fcmToken = new FCMToken();
            fcmToken.setLoggedIn(true);
            fcmToken.setAccount(account);
            fcmTokenRepository.save(fcmToken);
            account.setFCMToken(fcmToken);
        } else {
            // 기존 FCM 토큰이 있으면 로그인 상태를 true로 업데이트
            account.getFCMToken().setLoggedIn(true);
            fcmTokenRepository.save(account.getFCMToken());
        }
    }

}
