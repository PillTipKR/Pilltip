package com.oauth2.HealthSupplement.Review.Repository;

import com.oauth2.Drug.Review.Domain.Tag;
import com.oauth2.Drug.Review.Domain.TagType;
import com.oauth2.HealthSupplement.Review.Entity.SupplementTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplementTagRepository extends JpaRepository<SupplementTag, Long> {

    // 이름과 타입으로 태그 조회
    Optional<SupplementTag> findByNameAndType(String name, TagType type);

    List<SupplementTag> findByType(TagType type);
}
