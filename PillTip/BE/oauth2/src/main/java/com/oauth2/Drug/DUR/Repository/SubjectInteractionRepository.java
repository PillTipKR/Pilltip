package com.oauth2.Drug.DUR.Repository;

import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectInteractionRepository extends JpaRepository<SubjectInteraction, Long> {

    List<SubjectInteraction> findBySubjectId1AndDurtype1AndSubjectId2AndDurtype2(Long subjectId1, DurType durtype1, Long subjectId2, DurType durtype2);
    List<SubjectInteraction> findByDurtype1AndDurtype2(DurType durType1, DurType durType2);
} 