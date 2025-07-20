// author : mireutale
// description : 회원가입 서비스
package com.oauth2.Account.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Entity.AccountToken;
import com.oauth2.Account.Entity.LoginType;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.Account.Dto.CreateUserDto;
import com.oauth2.User.UserInfo.Dto.ProfileRequest;
import com.oauth2.Account.Dto.SignupRequest;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.User.UserInfo.Entity.*;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.Account.Dto.OAuth2UserInfo;
import com.oauth2.User.UserInfo.Service.UserProfileService;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.Util.Exception.CustomException.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service //스프링 서비스 빈으로 등록
@RequiredArgsConstructor //final 필드에 생성자 자동 생성
@Transactional //메서드 전체를 하나의 트랜잭션으로 처리
public class SignupService {
    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);
    private final UserRepository userRepository; //유저 저장소 접근 객체
    private final UserService userService; //유저 서비스
    private final PasswordEncoder passwordEncoder; //비밀번호 암호화
    private final TokenService tokenService;
    private final OAuth2Service oauth2Service;
    private final FCMTokenRepository fcmTokenRepository;
    private final AccountRepository accountRepository;
    private final UserProfileService userProfileService;

    //회원가입 요청 처리
    @Transactional
    public CreateUserDto signup(SignupRequest request) {
        logger.info("회원가입 시작 - LoginType: {}, Provider: {}, Nickname: {}", 
                   request.getLoginType(), request.getProvider(), request.getNickname());
        
        try {
            validateSignupRequest(request); //요청 유효성 검사
            logger.info("회원가입 요청 유효성 검사 통과");

            CreateUserDto createUserDto = createUser(request);
            Account account = createUserDto.account();
            User user = createUserDto.user();
            if (request.getLoginType() == LoginType.SOCIAL) {
                logger.info("소셜 로그인 회원가입 처리 시작 - Provider: {}", request.getProvider());
                
                // OAuth2 서버에서 사용자 정보 가져오기
                OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                        request.getProvider(),
                        request.getToken()
                );
                
                logger.info("OAuth2 사용자 정보 조회 성공 - SocialId: {}", oauth2UserInfo.getSocialId());

                user.setProfilePhoto(oauth2UserInfo.getProfileImage());  // null 가능

                account.setSocialId(oauth2UserInfo.getSocialId());
                account.setUserEmail(oauth2UserInfo.getEmail());  // null 가능

                logger.info("소셜 사용자 객체 생성 완료 - SocialId: {}, Email: {}, Nickname: {}", 
                           oauth2UserInfo.getSocialId(), oauth2UserInfo.getEmail(), request.getNickname());
            }

            logger.info("사용자 저장 시작");
            user.setAccount(account);
            List<User> users = new ArrayList<>();
            users.add(user);
            account.setUsers(users);

            account = accountRepository.save(account);
            logger.info("사용자 저장 완료 - UserId: {}", user.getId());

            logger.info("사용자 프로필 정보 생성 시작");
            ProfileRequest profileRequest = new ProfileRequest(
                    request.getNickname(),
                    request.getGender(),
                    request.getBirthDate(),
                    request.getAge(),
                    request.getHeight(),
                    request.getWeight(),
                    request.getPhone(),
                    request.getInterest()
            );
            userProfileService.userSetting(user, profileRequest,true);

            logger.info("사용자 토큰 생성 시작");
            AccountToken accountToken = tokenService.generateTokens(account.getId());
            account.setAccountToken(accountToken);
            logger.info("사용자 토큰 생성 완료");

            logger.info("FCM 토큰 생성 시작");
            FCMToken fcmToken = new FCMToken();
            fcmToken.setLoggedIn(true);
            fcmToken.setAccount(account);
            fcmTokenRepository.save(fcmToken);
            account.setFCMToken(fcmToken);
            logger.info("FCM 토큰 생성 완료");

            logger.info("최종 사용자 정보 저장");

            userRepository.save(user);
            accountRepository.save(account);
            logger.info("회원가입 완료 - UserId: {}, LoginType: {}", user.getId(), account.getLoginType());

            return new CreateUserDto(
                    account,
                    user
            );
        } catch (Exception e) {
            logger.error("회원가입 실패 - LoginType: {}, Provider: {}, Error: {}", 
                        request.getLoginType(), request.getProvider(), e.getMessage(), e);
            throw e;
        }
    }


    //회원가입 요청 유효성 검사
    private void validateSignupRequest(SignupRequest request) {
        logger.info("회원가입 요청 유효성 검사 시작");
        
        // loginType 검사
        if (request.getLoginType() == null) {
            logger.error("로그인 타입이 null입니다");
            throw new LoginTypeRequiredDetailException();
        }
        
        // IDPW 로그인, 빈 값 검사, 중복 검사
        if (request.getLoginType() == LoginType.IDPW) {
            logger.info("ID/PW 로그인 유효성 검사");
            if (request.getLoginId() == null || request.getPassword() == null) {
                logger.error("ID/PW 로그인에서 loginId 또는 password가 null입니다");
                throw new UserIdPasswordRequiredException();
            }
            logger.info("loginId 중복 검사 시작");
            userService.checkDuplicate(request.getLoginId(), "loginid");
            logger.info("loginId 중복 검사 완료");
        }
        // 소셜 로그인, 빈 값 검사, 중복 검사
        else if (request.getLoginType() == LoginType.SOCIAL) {
            logger.info("소셜 로그인 유효성 검사 - Provider: {}", request.getProvider());
            if (request.getToken() == null) {
                logger.error("소셜 로그인에서 토큰이 null입니다");
                throw new TokenRequiredForSocialException();
            }
            
            logger.info("OAuth2 사용자 정보 조회 시작 - Provider: {}", request.getProvider());
            // OAuth2 서버에서 사용자 정보 가져오기
            OAuth2UserInfo oauth2UserInfo = oauth2Service.getUserInfo(
                    request.getProvider(),
                    request.getToken()
            );
            logger.info("OAuth2 사용자 정보 조회 완료 - SocialId: {}", oauth2UserInfo.getSocialId());
            
            logger.info("socialId 중복 검사 시작");
            // socialId 중복 체크 (EncryptionConverter가 자동으로 복호화)
            List<Account> allAccount = accountRepository.findAll();
            for (Account account : allAccount) {
                if (account.getSocialId() != null && account.getSocialId().equals(oauth2UserInfo.getSocialId())) {
                    logger.error("이미 존재하는 소셜 계정입니다 - SocialId: {}", oauth2UserInfo.getSocialId());
                    throw new SocialAccountAlreadyExistsDetailException();
                }
            }
            logger.info("socialId 중복 검사 완료");
        }
        
        // 전화번호 중복 검사
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            logger.error("전화번호가 필수입니다");
            throw new RuntimeException("전화번호는 필수 입력 항목입니다.");
        }
        logger.info("전화번호 중복 검사 시작");
        userService.checkDuplicate(request.getPhone(), "phonenumber");
        logger.info("전화번호 중복 검사 완료");
        
        logger.info("회원가입 요청 유효성 검사 완료");
    }

    // 사용자 생성
    private CreateUserDto createUser(SignupRequest request) {
        return new CreateUserDto(
                Account.builder()
                        .loginType(request.getLoginType())
                        .loginId(request.getLoginType() == LoginType.IDPW ? request.getLoginId() : null)
                        .socialId(null) // 소셜 로그인의 경우 signup 메서드에서 별도로 설정
                        .passwordHash(request.getLoginType() == LoginType.IDPW ?
                                passwordEncoder.encode(request.getPassword()) : null)
                        .build(),
                User.builder()
                        .nickname(request.getNickname())
                        .profilePhoto(null)
                        .terms(false)
                        .build()
        );
    }

}
