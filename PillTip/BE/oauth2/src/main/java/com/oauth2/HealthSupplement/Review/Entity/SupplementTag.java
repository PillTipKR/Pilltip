package com.oauth2.HealthSupplement.Review.Entity;

import com.oauth2.Drug.Review.Domain.ReviewTag;
import com.oauth2.Drug.Review.Domain.TagType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "supplement_tags")
public class SupplementTag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TagType type; // EFFICACY, SIDE_EFFECT, OTHER

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementReviewTag> reviewTags = new ArrayList<>();

    public SupplementTag(String tagName, TagType type) {
        this.name = tagName;
        this.type = type;
    }

    public SupplementTag() {}

    // Getter/Setter, 생성자 등 생략
}

