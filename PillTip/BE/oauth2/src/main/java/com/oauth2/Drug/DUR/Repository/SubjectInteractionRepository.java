package com.oauth2.Drug.DUR.Repository;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectInteractionRepository extends JpaRepository<SubjectInteraction, Long> {


    @Query("""
select count(si) > 0 from SubjectInteraction si
where (
    (si.subjectId1 = :subjectId1 and si.durtype1 = :durtype1 and si.subjectId2 = :subjectId2 and si.durtype2 = :durtype2)
 or (si.subjectId1 = :subjectId2 and si.durtype1 = :durtype2 and si.subjectId2 = :subjectId1 and si.durtype2 = :durtype1)
)
""")
    boolean existsSymmetric(
            @Param("subjectId1") Long subjectId1,
            @Param("durtype1")   DurType durtype1,
            @Param("subjectId2") Long subjectId2,
            @Param("durtype2")   DurType durtype2
    );
    @Query("""
    select si from SubjectInteraction si
    where ((si.subjectId1 = :a and si.durtype1 = :dt1 and si.subjectId2 = :b and si.durtype2 = :dt2)
        or (si.subjectId1 = :b and si.durtype1 = :dt2 and si.subjectId2 = :a and si.durtype2 = :dt1))
    """)
    List<SubjectInteraction> findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(Long a, DurType dt1, Long b, DurType dt2);

    @Query("""
    select si from SubjectInteraction si
    where ((si.durtype1 = :dt1 and si.durtype2 = :dt2)
        or (si.durtype1 = :dt2 and si.durtype2 = :dt1))
    """)
    List<SubjectInteraction> findByDurtype1AndDurtype2(DurType dt1, DurType dt2);
} 