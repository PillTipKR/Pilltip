package com.oauth2.Drug.Review.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tags")
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TagType type; // EFFICACY, SIDE_EFFECT, OTHER

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewTag> reviewTags = new ArrayList<>();

    public Tag(String tagName, TagType type) {
        this.name = tagName;
        this.type = type;
    }

    public Tag() {}

    // Getter/Setter, 생성자 등 생략
}

