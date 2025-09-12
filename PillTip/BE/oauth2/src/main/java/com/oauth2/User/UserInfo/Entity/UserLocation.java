// author : mireutale
// description : 유저 위치 엔티티
package com.oauth2.User.UserInfo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_location")
public class UserLocation {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user; // 유저 위치 user_id를 user 테이블의 id와 매핑

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude; // 유저 위치 위도

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude; // 유저 위치 경도

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 유저 위치 생성 시간

    @Builder
    public UserLocation(User user, BigDecimal latitude, BigDecimal longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
