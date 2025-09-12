// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.Account.Repository.AccountTokenRepository;
import com.oauth2.Util.Exception.CustomException.DuplicateLoginIdException;
import com.oauth2.Util.Exception.CustomException.DuplicatePhoneFormatException;
import com.oauth2.Util.Exception.CustomException.InvalidCheckTypeException;
import com.oauth2.Util.Exception.CustomException.UserInfoNotFoundRetryLoginException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.oauth2.Account.Dto.AuthMessageConstants;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FCMTokenRepository fcmTokenRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final UserProfileService userProfileService;

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findByIdWithQuestionnaire(userId)
                .orElseThrow(UserInfoNotFoundRetryLoginException::new);
    }

    // 실명과 주소 업데이트
    public User updatePersonalInfo(User user, String realName, String address) {
        user.setRealName(realName);
        user.setAddress(address);
        return userRepository.save(user);
    }

    // 전화번호 업데이트 (UserProfile의 phone 필드 수정)
    public User updatePhoneNumber(User user, String phoneNumber) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(UserInfoNotFoundRetryLoginException::new);
        userProfile.setPhone(phoneNumber);
        userProfileRepository.save(userProfile);
        return user;
    }

    // 이용약관 동의
    public User agreeToTerms(User user) {
        user.setTerms(true);
        return userRepository.save(user);
    }

    // 닉네임 업데이트
    public User updateNickname(User user, String nickname) {
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    // 프로필 사진 업데이트
    public User updateProfilePhoto(User user, String photoUrl) {
        user.setProfilePhoto(photoUrl);
        return userRepository.save(user);
    }

    // 중복 체크 (예외 발생)
    public void checkDuplicate(String value, String type) {
        boolean isDuplicate = isDuplicate(value, type);
        if (isDuplicate) {
            switch (type.toLowerCase()) {
                case "loginid":
                    throw new DuplicateLoginIdException();
                case "phonenumber":
                    throw new DuplicatePhoneFormatException();
                default:
                    throw new InvalidCheckTypeException();
            }
        }
    }

    // 중복 체크 (boolean 반환)
    public boolean isDuplicate(String value, String type) {
        return switch (type.toLowerCase()) {
            case "loginid" -> checkLoginIdDuplicate(value);
            case "phonenumber" -> checkPhoneNumberDuplicate(value);
            default -> throw new IllegalArgumentException(AuthMessageConstants.INVALID_CHECK_TYPE + ": " + type);
        };
    }

    // loginId 중복 체크 (EncryptionConverter가 자동으로 복호화)
    private boolean checkLoginIdDuplicate(String loginId) {
        List<Account> allAccount = accountRepository.findAll();
        
        for (Account account : allAccount) {
            if (account.getLoginId() != null && account.getLoginId().equals(loginId)) {
                return true;
            }
        }
        return false;
    }

    // phoneNumber 중복 체크 (EncryptionConverter가 자동으로 복호화)
    private boolean checkPhoneNumberDuplicate(String phoneNumber) {
        List<UserProfile> allProfiles = userProfileRepository.findAll();
        
        for (UserProfile profile : allProfiles) {
            if (profile.getPhone() != null && profile.getPhone().equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }

    // 전화번호로 사용자 조회
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElse(null);
    }

    // 회원 탈퇴
    public void deleteAccount(Long accountId) {
        logger.info("회원 탈퇴 시작 - AccountId: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(UserInfoNotFoundRetryLoginException::new);
        try {
            // 1. FCM 토큰 삭제
            logger.info("FCM 토큰 삭제 시작");
            fcmTokenRepository.deleteByAccount(account);

            // 2. 사용자 토큰 삭제
            logger.info("jwt 토큰 삭제 시작");
            accountTokenRepository.deleteById(accountId);

            List<User> users = account.getUsers();
            for (User user : users) {
                userProfileService.deleteProfile(user.getId());
            }
            logger.info("회원 탈퇴 완료 - AccountId: {}", accountId);
            accountRepository.delete(account);
        } catch (Exception e) {
            logger.error("회원 탈퇴 실패 - AccountId: {}, Error: {}", accountId, e.getMessage(), e);
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
