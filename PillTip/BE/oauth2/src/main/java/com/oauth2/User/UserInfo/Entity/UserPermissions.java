// author : mireutale
// description : 유저 동의 엔티티
package com.oauth2.User.UserInfo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_permissions")
public class UserPermissions {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user; // 유저 동의 user_id를 user 테이블의 id와 매핑

    @Column(name = "location_permission", nullable = false)
    private boolean locationPermission; // 유저 위치 동의

    @Column(name = "camera_permission", nullable = false)
    private boolean cameraPermission; // 유저 카메라 동의

    @Column(name = "gallery_permission", nullable = false)
    private boolean galleryPermission; // 유저 갤러리 동의

    @Column(name = "phone_permission", nullable = false, columnDefinition = "boolean default true")
    private boolean phonePermission = true; // 유저 전화번호 동의

    @Column(name = "sms_permission", nullable = false)
    private boolean smsPermission; // 유저 문자 동의

    @Column(name = "file_permission", nullable = false)
    private boolean filePermission; // 유저 파일 동의

    // 문진표 관련 동의
    // 민감정보 수집 동의
    @Column(name = "sensitive_info_permission", nullable = false)
    private boolean sensitiveInfoPermission; // 유저 민감정보 수집 동의

    // 의약법 관련 동의
    @Column(name = "medical_info_permission", nullable = false)
    private boolean medicalInfoPermission; // 유저 의약법 동의

    @Column(name = "friend_permission", nullable = false)
    private boolean friendPermission; // 유저 친구 동의

    @Builder
    public UserPermissions(User user, boolean locationPermission, boolean cameraPermission,
                          boolean galleryPermission, boolean phonePermission, boolean smsPermission, boolean filePermission,
                          boolean sensitiveInfoPermission, boolean medicalInfoPermission, boolean friendPermission) {
        this.user = user;
        this.locationPermission = locationPermission;
        this.cameraPermission = cameraPermission;
        this.galleryPermission = galleryPermission;
        this.phonePermission = phonePermission;
        this.smsPermission = smsPermission;
        this.filePermission = filePermission;
        this.sensitiveInfoPermission = sensitiveInfoPermission;
        this.medicalInfoPermission = medicalInfoPermission;
        this.friendPermission = friendPermission;
    }
}
