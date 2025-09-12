package com.oauth2.Drug.Review.Repository;

import com.oauth2.Drug.Review.Domain.Tag;
import com.oauth2.Drug.Review.Domain.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 이름과 타입으로 태그 조회
    Optional<Tag> findByNameAndType(String name, TagType type);

    List<Tag> findByType(TagType type);
}
