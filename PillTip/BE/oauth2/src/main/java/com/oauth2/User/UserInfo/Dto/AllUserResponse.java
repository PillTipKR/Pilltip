package com.oauth2.User.UserInfo.Dto;

import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserPermissions;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import lombok.Getter;

import java.util.List;

@Getter
public class AllUserResponse{

    // 기본 사용자 정보
    private final Long id;
    private final String nickname;
    private final String profilePhoto;
    private final boolean terms;

    // UserProfile 정보
    private final Integer age;
    private final String gender;
    private final String birthDate;
    private final String phone;
    private final boolean pregnant;

    // UserProfile에서 height, weight
    private final String height;
    private final String weight;

    // 문진표 정보 (문진표가 있을 때만)
    private final String realName;
    private final String address;

    // 권한 정보 (두 권한이 모두 true일 때만 true)
    private final boolean permissions;
    private final Boolean friendPermission;

    private final List<UserListDto> userList;

    // 생성자, 앱에서 정보를 처리하기 쉽도록 일정한 형식으로 변환
    public AllUserResponse(User user, List<UserListDto> userList) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profilePhoto = user.getProfilePhoto(); // EncryptionConverter가 자동으로 복호화
        this.terms = user.getTerms();
        this.userList = userList;

        // UserProfile에서 정보 가져오기
        UserProfile profile = user.getUserProfile();
        this.age = profile != null ? profile.getAge() : null;
        this.gender = profile != null ? (profile.getGender() != null ? profile.getGender().name() : null) : null;
        this.birthDate = profile != null ? (profile.getBirthDate() != null ? profile.getBirthDate().toString() : null) : null;
        this.phone = profile != null ? profile.getPhone() : null; // EncryptionConverter가 자동으로 복호화
        this.pregnant = profile != null ? profile.isPregnant() : false;

        // height, weight
        this.height = profile != null && profile.getHeight() != null ? profile.getHeight().toString() : null;
        this.weight = profile != null && profile.getWeight() != null ? profile.getWeight().toString() : null;

        // User 엔티티에서 직접 realName과 address 가져오기 (EncryptionConverter가 자동으로 복호화)
        this.realName = user.getRealName();
        this.address = user.getAddress();

        // 권한 정보: 두 권한이 모두 true일 때만 true
        UserPermissions userPermissions = user.getUserPermissions();
        this.permissions = userPermissions != null &&
                userPermissions.isSensitiveInfoPermission() &&
                userPermissions.isMedicalInfoPermission();
        this.friendPermission = user.getFriendPermission() != null ? user.getFriendPermission() : false;

    }
}
