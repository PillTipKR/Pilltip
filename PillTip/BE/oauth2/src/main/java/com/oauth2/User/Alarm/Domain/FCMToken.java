package com.oauth2.User.Alarm.Domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.oauth2.Account.Entity.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class FCMToken {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account; // 유저 프로필 user_id를 user 테이블의 id와 매핑

    private String FCMToken;
    private boolean loggedIn;

    public FCMToken(String token) {
        FCMToken = token;
        loggedIn = true;
    }

    public FCMToken() {}
}
