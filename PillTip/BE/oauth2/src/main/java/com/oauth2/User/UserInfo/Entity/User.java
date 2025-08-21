// author : mireutale
// description : 유저 엔티티
package com.oauth2.User.UserInfo.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.oauth2.Drug.Review.Domain.Review;
import com.oauth2.Drug.Review.Domain.ReviewLike;
import com.oauth2.Account.Entity.Account;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingSupplement.Entity.TakingSupplement;
import com.oauth2.Util.Encryption.EncryptionConverter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity //JPA에서 DB테이블과 매핑되는 클래스임을 명시
@Table(name = "users") //DB에서 테이블 이름
@Getter // 모든 필드의 Getter 메서드 생성
@Setter
@NoArgsConstructor // 기본 생성자 생성
@Builder // 빌더 패턴 사용
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 생성
public class User {
    @Id // 기본키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Id에 대해서 AUTO_INCREMENT 수행
    private Long id;

    @Column(name = "profile_photo") // 유저의 프로필 사진 URL
    @Convert(converter = EncryptionConverter.class)
    private String profilePhoto;

    @Column(nullable = false) // 유저의 닉네임
    private String nickname;

    @Column(nullable = false) // 유저의 동의사항
    private Boolean terms;

    @Column(name = "real_name")
    @Convert(converter = EncryptionConverter.class)
    private String realName; // 실명 (null 허용)

    @Column(name = "address")
    @Convert(converter = EncryptionConverter.class)
    private String address; // 주소 (null 허용)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // onCreate에서 현재 시간을 가져오고, 이 값을 저장

    // 유저 프로필 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    // 유저 관심사 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Interests interests;

    // 유저 동의사항 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPermissions userPermissions;

    @Column(name = "friend_permission")
    private Boolean friendPermission;

    // 유저 위치 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserLocation userLocation;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewLike> likes = new ArrayList<>();

    // 유저 문진표 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PatientQuestionnaire questionnaire;

    // 복용 중인 약 1대 N 관계
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TakingPill> takingPills;

    // 복용 중인 약 1대 N 관계
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TakingSupplement> takingSupplements;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private boolean isMain;

    @PrePersist // 엔티티 저장 전 실행
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User update(String new_nickname, String new_profilePhoto) {
        this.nickname = new_nickname; // 닉네임 업데이트
        this.profilePhoto = new_profilePhoto; // 프로필 사진 업데이트
        return this; // 업데이트된 유저 반환
    }

    @Builder
    public User(Account account, String profilePhoto, String nickname, boolean terms, String realName, String address) {
        this.account = account;
        this.profilePhoto = profilePhoto;
        this.nickname = nickname;
        this.terms = terms;
        this.realName = realName;
        this.address = address;
    }
}
