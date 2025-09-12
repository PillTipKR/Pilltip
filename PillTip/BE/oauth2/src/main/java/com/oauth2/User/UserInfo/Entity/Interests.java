// author : mireutale
// description : 관심사 엔티티
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
@Table(name = "interests")
public class Interests {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @JsonBackReference
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean diet; // 다이어트

    @Column(nullable = false)
    private boolean health; // 건강

    @Column(nullable = false)
    private boolean muscle; // 운동

    @Column(nullable = false)
    private boolean aging; // 노화

    @Column(nullable = false)
    private boolean nutrient; // 영양   

    @Builder
    public Interests(User user, boolean diet, boolean health, boolean muscle, boolean aging, boolean nutrient) {
        this.user = user;
        this.diet = diet;
        this.health = health;
        this.muscle = muscle;
        this.aging = aging;
        this.nutrient = nutrient;
    }
}
