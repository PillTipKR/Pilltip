package com.oauth2.Drug.Review.Repository;

import com.oauth2.Drug.Review.Domain.ReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

    // 특정 리뷰에 연결된 태그들
    List<ReviewTag> findByReviewId(Long reviewId);

    // 특정 태그가 연결된 리뷰들
    List<ReviewTag> findByTagId(Long tagId);


    @Query("""
    SELECT t.name, t.type, COUNT(rt)
    FROM ReviewTag rt
    JOIN rt.review r
    JOIN rt.tag t
    WHERE r.drug.id = :drugId
    GROUP BY t.name, t.type
    ORDER BY COUNT(rt) DESC
    """)
    List<Object[]> countTagsByDrug(@Param("drugId") Long drugId);
}


