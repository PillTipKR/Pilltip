package com.oauth2.Drug.DUR.Repository;

import com.oauth2.Drug.DUR.Domain.ConditionType;
import com.oauth2.Drug.DUR.Domain.DurType;
import com.oauth2.Drug.DUR.Domain.SubjectCaution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubjectCautionRepository extends JpaRepository<SubjectCaution, Long> {
    List<SubjectCaution> findBySubjectIdAndConditionType(Long subjectId, ConditionType conditionType);
    List<SubjectCaution> findByDurtype(DurType durType);
    Boolean existsBySubjectIdAndDurtypeAndConditionType(Long subjectId, DurType durType, ConditionType conditionType);

}