package com.oauth2.User.UserInfo.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.Friend.Repository.FriendRepository;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.PatientQuestionnaire.Repository.QuestionnaireQRUrlRepository;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageScheduleRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import com.oauth2.User.UserInfo.Dto.ChildProfileRequest;
import com.oauth2.User.UserInfo.Dto.ProfileRequest;
import com.oauth2.User.UserInfo.Dto.UserResponse;
import com.oauth2.User.UserInfo.Entity.*;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import com.oauth2.User.UserInfo.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserSensitiveInfoRepository;
import com.oauth2.Util.Exception.CustomException.InvalidPhoneNumberFormatException;
import com.oauth2.Util.Exception.CustomException.ProfileIsMainException;
import com.oauth2.Util.Exception.CustomException.UserInfoNotFoundRetryLoginException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    private final UserProfileRepository userProfileRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final DosageLogRepository dosageLogRepository;
    private final FriendRepository friendRepository;
    private final TakingPillRepository takingPillRepository;
    private final DosageScheduleRepository dosageScheduleRepository;
    private final QuestionnaireQRUrlRepository questionnaireQRUrlRepository;
    private final PatientQuestionnaireRepository patientQuestionnaireRepository;
    private final UserSensitiveInfoRepository userSensitiveInfoRepository;

    public UserProfile getUserProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    public UserProfile updatePregnant(User user, boolean pregnant) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setPregnant(pregnant);
        return userProfileRepository.save(userProfile);
    }

    //회원가입 요청 처리
    @Transactional
    public UserResponse createProfile(ChildProfileRequest request, Long accountId) {

        try {
            User user = User.builder()
                    .nickname(request.nickname())
                    .profilePhoto(null)
                    .terms(false)
                    .build();
            Account account = accountRepository.findByIdWithUsers(accountId).orElse(null);
            user.setAccount(account);

            assert account != null;
            account.getUsers().add(user);
            logger.info("사용자 저장 완료 - UserId: {}", user.getId());

            logger.info("사용자 프로필 정보 생성 시작");
            profileSetting(user, request, false);

            logger.info("최종 사용자 정보 저장");

            userRepository.save(user);
            accountRepository.save(account);
            return new UserResponse(user);
        } catch (Exception e) {
            logger.error("프로필 생성 실패", e);
            throw e;
        }
    }

    private UserProfile createChildProfile(User user, ChildProfileRequest request) {
        return UserProfile.builder()
                .user(user)
                .age(request.age())
                .gender(Gender.valueOf(request.gender().toUpperCase()))
                .birthDate(LocalDate.parse(request.birthDate()))
                .height(new BigDecimal(request.height()))
                .weight(new BigDecimal(request.weight()))
                .phone("") // 전화번호는 받지 않음
                .build();
    }

    public void profileSetting(User user, ChildProfileRequest request, boolean isMain) {
        UserProfile userProfile = createChildProfile(user, request);
        Interests userInterests = createChildInterests(user);
        UserPermissions userPermissions = createUserPermissions(user);
        UserLocation userLocation = createUserLocation(user);

        user.setUserProfile(userProfile);
        user.setInterests(userInterests);
        user.setUserPermissions(userPermissions);
        user.setUserLocation(userLocation);
        user.setMain(isMain);

    }

    // 사용자 관심사 생성
    private Interests createChildInterests(User user) {
        return Interests.builder()
                .user(user)
                .diet(false)
                .health(false)
                .muscle(false)
                .aging(false)
                .nutrient(false)
                .build();
    }

    // 사용자 프로필 생성
    private UserProfile createUserProfile(User user, ProfileRequest request) {
        return UserProfile.builder()
                .user(user)
                .age(request.age())
                .gender(Gender.valueOf(request.gender().toUpperCase()))
                .birthDate(LocalDate.parse(request.birthDate()))
                .height(new BigDecimal(request.height()))
                .weight(new BigDecimal(request.weight()))
                .phone(validatePhoneNumber(request.phone())) // 전화번호는 이미 필수 검증됨
                .build();
    }

    // 사용자 관심사 생성
    private Interests createUserInterests(User user, ProfileRequest request) {
        Interests userInterests = Interests.builder()
                .user(user)
                .diet(false)
                .health(false)
                .muscle(false)
                .aging(false)
                .nutrient(false)
                .build();

        String interest = request.interest();
        if (interest != null && !interest.trim().isEmpty()) {
            String[] interestArray = interest.split(",");
            for (String interestItem : interestArray) {
                switch (interestItem.trim().toLowerCase()) {
                    case "diet":
                        userInterests.setDiet(true);
                        break;
                    case "health":
                        userInterests.setHealth(true);
                        break;
                    case "muscle":
                        userInterests.setMuscle(true);
                        break;
                    case "aging":
                        userInterests.setAging(true);
                        break;
                    case "nutrient":
                        userInterests.setNutrient(true);
                        break;
                }
            }
        }
        return userInterests;
    }

    // 사용자 권한 생성
    private UserPermissions createUserPermissions(User user) {
        return UserPermissions.builder()
                .user(user)
                .locationPermission(false)
                .cameraPermission(false)
                .galleryPermission(false)
                .phonePermission(false)
                .smsPermission(false)
                .filePermission(false)
                .sensitiveInfoPermission(false)
                .medicalInfoPermission(false)
                .build();
    }

    // 사용자 위치 생성
    private UserLocation createUserLocation(User user) {
        return UserLocation.builder()
                .user(user)
                .latitude(new BigDecimal("0.0"))
                .longitude(new BigDecimal("0.0"))
                .build();
    }


    public void userSetting(User user, ProfileRequest request, boolean isMain) {
        UserProfile userProfile = createUserProfile(user, request);
        Interests userInterests = createUserInterests(user, request);
        UserPermissions userPermissions = createUserPermissions(user);
        UserLocation userLocation = createUserLocation(user);

        user.setUserProfile(userProfile);
        user.setInterests(userInterests);
        user.setUserPermissions(userPermissions);
        user.setUserLocation(userLocation);
        user.setMain(isMain);

    }

    // 전화번호 유효성 검사
    private String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // 전화번호가 없으면 null 반환
        }
        if (!phone.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")) {
            throw new InvalidPhoneNumberFormatException();
        }
        return phone;
    }

    //  프로필 삭제
    @Transactional
    public void deleteProfile(Long userId) {
        logger.info("프로필 삭제 시작");
        User user = userRepository.findById(userId)
                .orElseThrow(UserInfoNotFoundRetryLoginException::new);
        if(user.isMain()) throw new ProfileIsMainException();
        try {
            // 1. 복용 로그 삭제 (TakingPill을 참조하므로 먼저 삭제)
            logger.info("복용 로그 삭제 시작");
            dosageLogRepository.deleteAllByUser(user);

            // 2. 복용 스케줄 삭제
            logger.info("복용 스케줄 삭제 시작");
            List<TakingPill> takingPills = takingPillRepository.findByUser(user);
            for (com.oauth2.User.TakingPill.Entity.TakingPill takingPill : takingPills) {
                dosageScheduleRepository.deleteAllByTakingPill(takingPill);
            }

            // 3. 복용 중인 약 삭제
            logger.info("복용 중인 약 삭제 시작");
            takingPillRepository.deleteAllByUser(user);

            // 4. 문진표 QR URL 삭제
            logger.info("문진표 QR URL 삭제 시작");
            questionnaireQRUrlRepository.deleteByUser(user);

            // 5. 문진표 삭제
            logger.info("문진표 삭제 시작");
            patientQuestionnaireRepository.deleteByUser(user);

            // 6. 사용자 민감정보 삭제
            logger.info("사용자 민감정보 삭제 시작");
            userSensitiveInfoRepository.deleteByUser(user);

            // 7. 친구 관계 삭제
            logger.info("친구 관계 삭제 시작");
            friendRepository.deleteAllByUserId(user.getId());
            friendRepository.deleteAllByFriendId(user.getId());

            // 8. 최종적으로 사용자 삭제 - account와 연결끊기
            User latestUser = userRepository.findById(userId)
                    .orElseThrow(UserInfoNotFoundRetryLoginException::new);

            // 9. 연관 관계 제거 (Account, Friends 등)
            if (latestUser.getAccount() != null) {
                latestUser.getAccount().getUsers().remove(latestUser);
                latestUser.setAccount(null);
            }

            // 11. 실제 삭제
            logger.info("프로필 삭제");
            userRepository.delete(latestUser);

        } catch (Exception e) {
            logger.error("프로필 삭제 실패 - UserId: {}, Error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("삭제중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}

