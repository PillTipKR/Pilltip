// author : mireutale
// description : API 컨트롤러

package com.oauth2.Account.Controller;

import com.oauth2.Account.Dto.*;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Account.Service.LoginService;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.Util.Exception.CustomException.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.Account.Service.SignupService;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Account.Service.TokenService;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AccountService accountService;
    private final SignupService signupService;
    private final TokenService tokenService;
    private final FCMTokenRepository fcmTokenRepository;
    private final LoginService loginService;

    // ID/PW 로그인 또는 소셜 로그인 (자동 감지)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        System.out.println("=== 로그인 API 호출 ===");
        System.out.println("LoginId: " + request.loginId());
        System.out.println("Password: " + (request.password() != null ? "***" : "null"));
        
        try {
            // loginId가 null이고 password가 null이면 소셜 로그인으로 간주
            if (request.loginId() == null && request.password() == null) {
                System.out.println("ERROR: 소셜 로그인 요청이 /api/auth/login으로 전송되었습니다. /api/auth/social-login을 사용해주세요.");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("소셜 로그인은 /api/auth/social-login API를 사용해주세요.", null));
            } else {
                // 일반 ID/PW 로그인
                System.out.println("일반 ID/PW 로그인 처리 중...");
                LoginResponse loginResponse = loginService.login(request);
                System.out.println("로그인 성공!");
                return ResponseEntity.status(200)
                    .body(ApiResponse.success(AuthMessageConstants.LOGIN_SUCCESS, loginResponse));
            }
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
            e.printStackTrace();
            throw new LoginFailedException();
        }
    }

    // 소셜 로그인
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        System.out.println("=== 소셜 로그인 API 호출됨! ===");
        System.out.println("Provider: " + request.getProvider());
        System.out.println("Token: " + (request.getToken() != null ? request.getToken().substring(0, Math.min(10, request.getToken().length())) + "..." : "null"));
        System.out.println("전체 요청: " + request.toString());
        System.out.println("요청 타임스탬프: " + System.currentTimeMillis());
        
        try {
            System.out.println("소셜 로그인 처리 중...");
            LoginResponse loginResponse = loginService.socialLogin(request);
            System.out.println("소셜 로그인 성공 - Provider: " + request.getProvider());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.SOCIAL_LOGIN_SUCCESS, loginResponse));
        } catch (Exception e) {
            System.out.println("소셜 로그인 실패 - Provider: " + request.getProvider() + ", Error: " + e.getMessage());
            e.printStackTrace();
            throw new LoginFailedException();
        }
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        System.out.println("=== 회원가입 API 호출 ===");
        System.out.println("LoginType: " + request.getLoginType());
        System.out.println("Provider: " + request.getProvider());
        System.out.println("Nickname: " + request.getNickname());
        
        try {
            System.out.println("SignupService.signup() 호출 시작");
            CreateUserDto createUserDto = signupService.signup(request);
            User user = createUserDto.user();
            System.out.println("SignupService.signup() 완료 - UserId: " + user.getId());

            System.out.println("토큰 생성 시작");
            Account account = createUserDto.account();
            AccountToken accountToken = tokenService.generateTokens(account.getId());
            System.out.println("토큰 생성 완료");

            SignupResponse signupResponse = SignupResponse.builder()
                    .accessToken(accountToken.getAccessToken())
                    .refreshToken(accountToken.getRefreshToken())
                    .build();

            return ResponseEntity.status(201)
                .body(ApiResponse.success(AuthMessageConstants.SIGNUP_SUCCESS, signupResponse));
        } catch (Exception e) {
            System.out.println("예상치 못한 회원가입 에러: " + e.getMessage());
            throw new SignupFailedException();
        }
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.isDuplicate(request.value(), request.type());
        String message = isDuplicate ? 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_FAILED, request.type()) : 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_SUCCESS, request.type());
        return ResponseEntity.ok()
            .body(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그아웃
    @PutMapping("/logout")
    public ResponseEntity<ApiResponse<String>> Logout(@AuthenticationPrincipal Account account) {
        if (account == null) throw new UserNotAuthenticatedException();

        try {
            
            if (account.getFCMToken() != null) {
                account.getFCMToken().setLoggedIn(false);
                fcmTokenRepository.save(account.getFCMToken());
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.success(AuthMessageConstants.LOGOUT_SUCCESS));
        } catch (Exception e) {
            System.out.println("Error during logout for user: " + account.getId() + " - Error: " + e.getMessage());
            throw new LogoutFailedException();
        }
    }

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = loginService.refreshToken(refreshToken);
            return ResponseEntity.ok()
                .body(ApiResponse.success(AuthMessageConstants.TOKEN_REFRESH_SUCCESS, loginResponse));
        } catch (RuntimeException e) {
            System.out.println("Token refresh failed: " + e.getMessage());
            throw new TokenRefreshFailedException();
        }
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<TermsResponse>> agreeToTerms(
            @AuthenticationPrincipal Account account,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws AccessDeniedException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        try {
            User updatedUser = userService.agreeToTerms(user);
            TermsResponse termsResponse = TermsResponse.builder()
                .terms(updatedUser.getTerms())
                .nickname(updatedUser.getNickname())
                .build();
            return ResponseEntity.ok()
                .body(ApiResponse.success(AuthMessageConstants.TERMS_AGREEMENT_SUCCESS, termsResponse));
        } catch (Exception e) {
            System.out.println("Terms agreement failed: " + e.getMessage());
            throw new TermsAgreementFailedException();
        }
    }
}
