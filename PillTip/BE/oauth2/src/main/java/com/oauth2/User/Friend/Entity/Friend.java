package com.oauth2.User.Friend.Entity;

import com.oauth2.User.UserInfo.Entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    private LocalDateTime createdAt;

    public Friend(User a, User b, LocalDateTime now) {
        this.user = a;
        this.friend = b;
        this.createdAt = now;
    }

    public Friend() {}
}

