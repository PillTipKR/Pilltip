package com.oauth2.Account.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Util.Encryption.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity //JPA에서 DB테이블과 매핑되는 클래스임을 명시
@Table(name = "accounts") //DB에서 테이블 이름
@Getter // 모든 필드의 Getter 메서드 생성
@Setter
@NoArgsConstructor // 기본 생성자 생성
@Builder // 빌더 패턴 사용
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 생성
public class Account {
    @Id // 기본키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Id에 대해서 AUTO_INCREMENT 수행
    private Long id;

    @Enumerated(EnumType.STRING) // 열거형 타입 지정
    @Column(name = "login_type", nullable = false) // 로그인 타입 지정, social or idpw
    private LoginType loginType;

    @Column(name = "login_id", unique = true) // 로그인 ID
    @Convert(converter = EncryptionConverter.class)
    private String loginId;

    @Column(name = "social_id", unique = true) // 소셜 로그인 Oauth2의 토큰
    @Convert(converter = EncryptionConverter.class)
    private String socialId;

    @Column(name = "password_hash", unique = true) // 로그인 비밀번호 hash
    private String passwordHash;

    @Column(name = "user_email", unique = true) // 유저의 이메일
    @Convert(converter = EncryptionConverter.class)
    private String userEmail;

    // 유저 토큰 1대 1 관계
    @JsonManagedReference
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private AccountToken accountToken;

    @JsonManagedReference
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private FCMToken FCMToken;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

}
